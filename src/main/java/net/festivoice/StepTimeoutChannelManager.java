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
import java.util.*;
import java.net.*;
import java.io.*;

public class StepTimeoutChannelManager extends Thread implements IChannelManager
{
	private LinkedHashMap<String, ChannelInfo> channels;
	private LinkedHashMap<String, ChannelInfo> channelsCache;

	private int interval;
	private int limit;
	private PrintStream logStream;

	/**
	 * @param interval_millis タイムアウトカウントをインクリメントする間隔(単位はミリ秒)
	 * @param limit_step タイムアウトカウントの上限
	 *
	 */
	StepTimeoutChannelManager(int interval_millis, int limit_step, PrintStream logStream)
	{
		this.logStream = logStream;
		channels = new LinkedHashMap<String, ChannelInfo>();
		updateChannelsCache();
		interval = interval_millis;
		limit = limit_step;
	}

	public Iterable<? extends IChannelInfo> getChannels()
	{
		return channelsCache.values();
	}

	public IChannelInfo channelData(String channelName,
			String userName, SocketAddress userAddress)
	{
//System.out.println("channelName channel:"+channelName+" user:"+userName+" addr:"+userAddress);
		ChannelInfo channel = channelsCache.get(channelName);
		if(channel == null) {
			synchronized(channels) {
				channel = channels.get(channelName);
				if(channel == null) {
					channel = new ChannelInfo(channelName);
					channels.put(channelName, channel);
					updateChannelsCache();
					logStream.println("create: '"+channelName+"'");
				}
			}
		}
		boolean updated = channel.userData(userName, userAddress);
		if(updated) {
			logStream.println("join: "+userAddress+" '"+channelName+"' <- '"+userName+"'");
		}
		return channel;
	}

	private void updateChannelsCache()
	{
		channelsCache = (LinkedHashMap)channels.clone();
	}


	public int getInterval() { return interval; }
	public void setInterval(int millis) { interval = millis; }

	public int getTimeoutLimit() { return limit; }
	public void setTimeoutLimit(int steps) { limit = steps; }

	public void run()
	{
		while(true) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException ignore) { }
			stepTimeout();
		}
	}

	private void stepTimeout()
	{
		boolean updated = false;
		synchronized(channels) {
			Iterator<ChannelInfo> it = channels.values().iterator();
			while(it.hasNext()) {
				ChannelInfo channel = it.next();
				Iterator<UserInfo> removed = channel.stepTimeout(getTimeoutLimit());

				if(removed != null) {
					updated = true;
					String channelName = channel.getChannelName();
					while(removed.hasNext()) {
						UserInfo user = removed.next();
						logStream.println("leave: "+user.getSocketAddress()+" '"+channelName+"' -> '"+user.getUserName()+"'");
					}
					if(channel.isEmpty()) {
						it.remove();
						logStream.println("remove: '"+channelName+"'");
					}
				}
			}

			if(updated) {
				updateChannelsCache();
			}
		}
	}
}

