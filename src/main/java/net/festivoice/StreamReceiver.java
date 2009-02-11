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
import javax.sound.sampled.*;

class LazyThreadedLineOut extends LazyInstance<ThreadedLineOut> { }

public class StreamReceiver extends Thread
{
	private static final int THREAD_LINEOUT = 256;
		
	private DatagramSocket socket;
	private DataLine.Info lineInfo;
	private ThreadedLineOut.CodecInfo codecInfo;
	private LazyThreadedLineOut[] threadedLines;
	private ClientUserInfoManager clientUserInfoManager;
	private boolean endFlag = false;

	public StreamReceiver(DatagramSocket socket, DataLine.Info lineInfo, ThreadedLineOut.CodecInfo codecInfo, ClientUserInfoManager clientUserInfoManager)
	{
		this.socket = socket;
		this.lineInfo = lineInfo;
		this.codecInfo = codecInfo;
		this.clientUserInfoManager = clientUserInfoManager;
		threadedLines = new LazyThreadedLineOut[THREAD_LINEOUT];
		for(int i=0; i < threadedLines.length; ++i) {
			threadedLines[i] = new LazyThreadedLineOut();
		}
	}

	public void run()
	{
		byte[] buf = new byte[1024 * 35];
		DatagramPacket received = new DatagramPacket(buf, buf.length);
		while (!endFlag) {
			try {
				received.setLength(buf.length);
				socket.receive(received);
				UDPData data = UDPData.deserialize(received.getData());
				clientUserInfoManager.user(data.getUserName(), data.getUserIndex());

				LazyThreadedLineOut line = threadedLines[data.getUserIndex()];
				synchronized(line) {
					if(!line.isInitialized()) {
						line.init(new ThreadedLineOut(lineInfo, codecInfo));
						line.getInstance().start();
					}
				}
				line.getInstance().put(data.getVoiceData());
			} catch (Exception e) {
			}
		}
	}

	public void end()
	{
		endFlag = true;
	}
}

