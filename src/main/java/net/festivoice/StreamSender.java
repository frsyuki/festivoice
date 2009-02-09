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

import java.lang.*;
import java.net.*;
import java.util.concurrent.*;
import org.xiph.speex.SpeexEncoder;
import javax.sound.sampled.*;

public class StreamSender extends Thread
{

	private ThreadedPacketSender.SessionInfo sessionInfo;

	private TargetDataLine targetDataLine;

	private ThreadedPacketSender[] packetSender;
	private BlockingQueue<byte[]> blockingQueue;

	private int baseByteArraySize;
	private CodecInfo codecInfo;

	private boolean endFlag = false;

	public static class CodecInfo
	{
		public AudioFormat format;
		public int mode;
		public int quality;
		public boolean vbr;
		public int countFrames;

		public CodecInfo(AudioFormat format, int mode, int quality, boolean vbr, int countFrames)
		{
			this.format = format;
			this.mode = mode;
			this.quality = quality;
			this.vbr = vbr;
			this.countFrames = countFrames;
		}
	}

	public StreamSender(ThreadedPacketSender.SessionInfo sessionInfo, TargetDataLine targetDataLine, CodecInfo codecInfo) throws LineUnavailableException
	{
		this.blockingQueue = new LinkedBlockingQueue<byte[]>(16*8);

		packetSender = new ThreadedPacketSender[1];
		for(int i=0; i < packetSender.length; ++i) {
			packetSender[i] = new ThreadedPacketSender(sessionInfo, codecInfo, blockingQueue);
		}
		this.codecInfo = codecInfo;

		// DataLine
		this.targetDataLine = targetDataLine;

		// Sound buffer
		baseByteArraySize = packetSender[0].getBaseArraySize();
	}

	public int getDecodeBufferSize()
	{
		return baseByteArraySize * codecInfo.countFrames;
	}

	public void setListenOnly(boolean flag)
	{
		for(ThreadedPacketSender s : packetSender) {
			s.setListenOnly(flag);
		}
	}

	public void end()
	{
		endFlag = true;
	}

	public void run()
	{
		for(ThreadedPacketSender s : packetSender) {
			s.start();
		}
		try {
			while (!endFlag) {
				// Input voice data
				byte[] soundBuffer = new byte[baseByteArraySize * codecInfo.countFrames];
				targetDataLine.read(soundBuffer, 0, baseByteArraySize);
				for (int i = 1; i < codecInfo.countFrames; i++) {
					targetDataLine.read(soundBuffer, baseByteArraySize * i, baseByteArraySize);
				}

				// put
				blockingQueue.put(soundBuffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

