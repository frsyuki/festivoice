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

interface IChannelManager
{
	/**
	 * チャンネルの一覧のスナップショットを返す。スレッドセーフ。
	 * @param data バッファのサイズ
	 * @return チャンネルの一覧
	 */
	public Iterable<? extends IChannelInfo> getChannels();

	/**
	 * チャンネルにユーザーが登録されていなければ登録し、されていればタイムアウト
	 * しないようにする(keepalive)。チャンネル名に結びつけられたチャンネル情報を
	 * 返す。スレッドセーフ。
	 * @param channelName 宛先のチャンネル名
	 * @param userName ストリームを送ってきたユーザーの名前
	 * @param userAddress ストリームを送ってきたユーザーのアドレス
	 * @return channelNameに対応するチャンネルの情報
	 */
	public IChannelInfo channelData(String channelName,
			String userName, InetSocketAddress userAddress);
}

