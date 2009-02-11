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
import org.xiph.speex.SpeexEncoder;
import javax.sound.sampled.*;

public class Client extends Thread
{
	private static final int LIMIT_TIME = 5;

	private StreamSender sender;
	private StreamReceiver receiver;

	private AudioFormat format;
	private InetSocketAddress serverAddress;
	private TargetDataLine inputDataLine;

	private ClientUserInfoManager clientUserInfoManager;
	private boolean endFlag = false;

	private static final int COUNT_FRAMES = 4;

	private Runnable initCallback;
	private boolean isMicAvailable = true;

	public Client(String channelName, String userName, InetSocketAddress serverAddress,
			int mode, int quality, boolean vbr) throws Exception
	{
		format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000.0F, 16, 1, 2, 16000.0F, false);

		clientUserInfoManager = new ClientUserInfoManager();

		DatagramSocket socket = new DatagramSocket();

		// Initialize StreamSender
		try {
			DataLine.Info inputDataLineInfo = new DataLine.Info(TargetDataLine.class, format);
			inputDataLine = (TargetDataLine)AudioSystem.getLine(inputDataLineInfo);
			inputDataLine.open();
		} catch (Exception e) {
			isMicAvailable = false;
		}

		ThreadedPacketSender.SessionInfo sessionInfo =
			new ThreadedPacketSender.SessionInfo(serverAddress, socket, channelName, userName);

		StreamSender.CodecInfo inputCodecInfo =
			new StreamSender.CodecInfo(format, mode, quality, vbr, COUNT_FRAMES);

		sender = new StreamSender(sessionInfo, inputDataLine, inputCodecInfo);

		// Initialize StreamReceiver
		ThreadedLineOut.CodecInfo outputCodecInfo =
			new ThreadedLineOut.CodecInfo(format, mode, sender.getDecodeBufferSize(), COUNT_FRAMES);

		DataLine.Info outputLineInfo = new DataLine.Info(SourceDataLine.class, format);
		receiver = new StreamReceiver(socket, outputLineInfo, outputCodecInfo, clientUserInfoManager);
	}

	public void setListenOnly(boolean flag)
	{
		if(!isMicAvailable) { flag = true; }
		sender.setListenOnly(flag);
	}

	public boolean isMicAvailable()
	{
		return this.isMicAvailable;
	}

	public void run()
	{
		try {
			if(isMicAvailable) {
				// Start input line
				inputDataLine.start();

				//while (!inputDataLine.isRunning()) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//}
			}

		} catch (Exception e) {
			isMicAvailable = false;
		}

		// Start StreamSender
		if(!isMicAvailable) {
			sender.setListenOnly(true);
		}
		sender.start();

		// Start StreamReceiver
		receiver.start();

		if(initCallback != null) {
			initCallback.run();
		}

		while(!endFlag) {
			try {
				clientUserInfoManager.stepTimeout(LIMIT_TIME);
				Thread.sleep(500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Stop data source
		try {
			inputDataLine.stop();
		} catch (Exception e) {
			// ignore error
		}
	}

	public void end()
	{
		endFlag = true;
	}

	public Iterable<? extends IClientUserInfo> getClientUserInfoIterator()
	{
		return clientUserInfoManager.getClientUserInfoIterator();
	}

	public void setInitCallback(Runnable initCallback)
	{
		// 初期化が完了したときに呼ぶ
		this.initCallback = initCallback;
	}

	public void setUserUpdateCallback(Runnable userUpdateCallback)
	{
		// ユーザー一覧が更新されたときに呼ぶ
		clientUserInfoManager.setUserUpdateCallback(userUpdateCallback);
	}
}


