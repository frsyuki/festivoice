/*
 * festivoice
 *
 * Copyright 2009 FURUHASHI Sadayuki, KASHIHARA Shuzo, SHIBATA Yasuharu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.festivoice;

import java.util.*;
import java.lang.*;
import java.net.*;
import java.util.concurrent.*;
import javax.sound.sampled.*;
import org.xiph.speex.SpeexEncoder;

public class ThreadedPacketSender extends Thread
{
	private BlockingQueue<byte[]> queue;
	private SessionInfo sessionInfo;
	private StreamSender.CodecInfo codecInfo;
	private SpeexEncoder encoder;
	private int baseByteArraySize;
	private boolean endFlag = false;

	public static class SessionInfo
	{
		private SocketAddress serverAddress;
		private DatagramSocket socket;
		private String channelName;
		private String userName;

		public SessionInfo(SocketAddress serverAddress, DatagramSocket socket, String channelName, String userName)
	   	{
			this.serverAddress = serverAddress;
			this.socket = socket;
			this.channelName = channelName;
			this.userName = userName;
		}
	}

	public ThreadedPacketSender(SessionInfo sessionInfo, StreamSender.CodecInfo codecInfo, BlockingQueue<byte[]> queue)
	{
		this.sessionInfo = sessionInfo;
		this.codecInfo = codecInfo;
		this.queue = queue;

		// Initialize Speex Encoder
		encoder = new SpeexEncoder();
		encoder.init(codecInfo.mode, codecInfo.quality, (int)codecInfo.format.getSampleRate(), codecInfo.format.getChannels(), codecInfo.vbr);
		// 内部では、byteからfloatに変換しているため、2倍する必要がある
		baseByteArraySize = encoder.getFrameSize() * encoder.getChannels() * 2;
	}

	public int getBaseArraySize()
	{
		return baseByteArraySize;
	}

	public void end()
	{
		endFlag = true;
	}

	public void run()
	{
		try {
			while(!endFlag) {
				byte[] soundBuffer = queue.take();
	
				// Encode voice data
				encoder.processData(soundBuffer, 0, baseByteArraySize);
				for (int i = 1; i < codecInfo.countFrames; i++) {
					encoder.processData(soundBuffer, baseByteArraySize * i, baseByteArraySize);
				}
				byte[] encodedData = new byte[encoder.getProcessedDataByteSize()];
				encoder.getProcessedData(encodedData, 0);
	
				// Pack and Send
				UDPData data = new UDPData(sessionInfo.channelName, sessionInfo.userName, encodedData, 0);	// FIXME: set sequence number
				byte[] buffer = data.toByteArray();
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, sessionInfo.serverAddress);
				sessionInfo.socket.send(packet);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

