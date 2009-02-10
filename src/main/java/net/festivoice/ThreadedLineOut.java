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
import org.xiph.speex.SpeexDecoder;

public class ThreadedLineOut extends Thread
{
	private SourceDataLine line;
	private CodecInfo codecInfo;
	private BlockingQueue<byte[]> queue;
	private SpeexDecoder decoder;
	private byte[] decodedData;
	private boolean endFlag = false;

	public static class CodecInfo
	{
		public AudioFormat format;
		public int mode;
		public int decodedSize;
		public int countFrames;

		public CodecInfo(AudioFormat format, int mode, int decodedSize, int countFrames)
		{
			this.format = format;
			this.mode = mode;
			this.decodedSize = decodedSize;
			this.countFrames = countFrames;
		}
	}

	public ThreadedLineOut(DataLine.Info info, CodecInfo codecInfo) throws Exception
	{
		this.codecInfo = codecInfo;
		line = (SourceDataLine)AudioSystem.getLine(info);
		line.open(codecInfo.format);

		decoder = new SpeexDecoder();
		decoder.init(codecInfo.mode, (int)codecInfo.format.getSampleRate(), codecInfo.format.getChannels(), false);

		queue = new LinkedBlockingQueue<byte[]>(16);

		decodedData = new byte[codecInfo.decodedSize];

		endFlag = false;
	}

	public void put(byte[] buffer) throws Exception
	{
		queue.put(buffer);
	}

	public void run()
	{
		line.start();
		try {
			while(!endFlag) {
				byte[] buffer = queue.take();

				if(buffer.length == 0) {
					// listen only packet
					continue;
				}
	
				decoder.processData(buffer, 0, buffer.length);
	
				for (int i = 1; i < codecInfo.countFrames; i++) {
					decoder.processData(false);
				}
	
				int decsize = decoder.getProcessedData(decodedData, 0);
				line.write(decodedData, 0, decsize);
			}
		} catch (Exception e) {
			System.out.println("line out error: "+e);
			e.printStackTrace();
		}
		line.stop();
	}

	public void end()
	{
		endFlag = true;
	}
}

