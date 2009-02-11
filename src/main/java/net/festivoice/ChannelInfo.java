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

public class ChannelInfo implements IChannelInfo
{
	private String name;
	private HashMap<InetSocketAddress, UserInfo> users;
	private HashMap<InetSocketAddress, UserInfo> usersCache;

	/**
	 * @param n チャンネル名
	 */
	ChannelInfo(String n)
	{
		name = n;
		users = new LinkedHashMap<InetSocketAddress, UserInfo>();
		updateUsersCache();
	}

	public String getChannelName()
	{
		return name;
	}

	public Iterable<? extends IUserInfo> getUsers()
	{
		return usersCache.values();
	}

	/**
	 * ユーザーが登録されていなければ登録し、されていれば
	 * タイムアウトカウントをリセットする(keepalive)。
	 * @return ユーザーが新規登録されたらtrue
	 */
	public boolean userData(String name, InetSocketAddress addr)
	{
	//	UserInfo user = new UserInfo(name, addr);
		// 新規優先
		//user = users.put(addr, user);
		//return user == null;
		// 新規優先順番重視
	//	user = users.putIfAbsent(addr, user);
	//	if(user != null) {
	//		user.userData(name);
	//		return false;
	//	} else {
	//		return true;
	//	}
		// 既存優先
		//user = users.putIfAbsent(user, addr);
		//if(user != null) {
		//	user.resetTimeout();
		//	return false;
		//} else {
		//	return true;
		//}
		UserInfo userInfo = usersCache.get(addr);
		if(userInfo != null) {
			userInfo.userData(name);
			return false;
		}

		synchronized(users) {
			userInfo = users.get(addr);
			if(userInfo != null) {
				userInfo.userData(name);
				return false;
			}

			userInfo = new UserInfo(addr, name);
			users.put(addr, userInfo);
			updateUsersCache();
			return true;
		}
	}

	private void updateUsersCache()
	{
		usersCache = (LinkedHashMap)users.clone();
	}

	/**
	 * タイムアウトカウントをインクリメントし、limitに達したユーザーを削除する。
	 * @return ユーザーが１人でも削除されたらnot nil
	 */
	public Iterator<UserInfo> stepTimeout(int limit)
	{
		boolean updated = false;
		List<UserInfo> removed = null;

		synchronized(users) {
			for(Iterator<UserInfo> it = users.values().iterator(); it.hasNext(); ) {
				UserInfo user = it.next();

				if(user.stepTimeout() > limit) {
					if(removed == null) {
						removed = new ArrayList<UserInfo>();
					}
					removed.add(user);
					it.remove();
				}
			}

			if(removed != null) {
				updateUsersCache();
				return removed.iterator();
			}

			return null;
		}
	}

	public boolean isEmpty()
	{
		return usersCache.isEmpty();
	}
}

