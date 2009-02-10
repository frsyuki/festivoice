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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPData {
	private int sequenceNumber;
	private short userIndex;
	private String userName;
	private String channelName;
	private byte[] voiceData;

	private UDPData() {
		sequenceNumber = 0;
		userIndex = 0;
		userName = "";
		channelName = "";
	}

	public String getChannelName() {
		return channelName;
	}
	public String getUserName() {
		return userName;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public short getUserIndex() {
		return userIndex;
	}
	public byte[] getVoiceData() {
		return voiceData;
	}

	public static byte[] serialize(String userName,String channelName,byte[] voiceData,int sequenceNumber,short userIndex) throws Exception {
		byte[] userNameBytes = userName.getBytes("UTF-8");
		byte[] channelNameBytes = channelName.getBytes("UTF-8");

		int datalen = 4 + 2 + 2 + userNameBytes.length + 2 + channelNameBytes.length + 2 + voiceData.length;

		ByteBuffer buf = ByteBuffer.allocate(datalen);
		buf.order(ByteOrder.BIG_ENDIAN);

		buf.putInt(sequenceNumber);
		buf.putShort(userIndex);

		buf.putShort((short)channelNameBytes.length);
		buf.put(channelNameBytes);

		buf.putShort((short)userNameBytes.length);
		buf.put(userNameBytes);

		buf.putShort((short)voiceData.length);
		buf.put(voiceData);

		byte[] result = new byte[buf.position()];
		System.arraycopy(buf.array(), 0, result, 0, result.length);

		return result;
	}

	public static UDPData deserialize(byte[] rawData) throws Exception {
		UDPData data = new UDPData();

		ByteBuffer buf = ByteBuffer.wrap(rawData);
		buf.order(ByteOrder.BIG_ENDIAN);

		data.sequenceNumber = buf.getInt();
		data.userIndex = buf.getShort();

		short chLen = buf.getShort();
		byte[] chbuf = new byte[chLen];
		buf.get(chbuf, 0, chLen);
		data.channelName = new String(chbuf, "UTF-8");

		short unameLen = buf.getShort();
		byte[] strbuf = new byte[unameLen];
		buf.get(strbuf, 0, unameLen);
		data.userName = new String(strbuf, "UTF-8");

		short voiceLen = buf.getShort();
		data.voiceData = new byte[voiceLen];
		buf.get(data.voiceData, 0, voiceLen);

		return data;
	}
}

