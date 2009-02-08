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
import java.util.*;

public class ClientUserInfoManager
{
	private LinkedHashMap<Integer, ClientUserInfo> users;
	private LinkedHashMap<Integer, ClientUserInfo> usersCache;
	private Runnable userUpdateCallback;

	public ClientUserInfoManager()
	{
		users = new LinkedHashMap<Integer, ClientUserInfo>();
		updateUsersCache();
	}

	public void setUserUpdateCallback(Runnable userUpdateCallback)
	{
		this.userUpdateCallback = userUpdateCallback;
	}

	public void user(String userName, short userIndex)
	{
		Integer index = Integer.valueOf(userIndex);

		ClientUserInfo userInfo = usersCache.get(index);
		if(userInfo != null) {
			userInfo.resetUser(userName);
			return;
		}

		synchronized(users) {
			userInfo = users.get(index);
			if(userInfo != null) {
				userInfo.resetUser(userName);
				return;
			}

			userInfo = new ClientUserInfo(userName, userIndex);
			users.put(index, userInfo);
			updateUsersCache();
		}
	}

	public boolean stepTimeout(int limit)
	{
		boolean updated = false;

		synchronized(users) {
			for (Iterator<ClientUserInfo> it = users.values().iterator(); it.hasNext(); ) {
				if (limit < it.next().stepTimeout()) {
					it.remove();
					updated = true;
				}
			}

			if (updated) {
				updateUsersCache();
			}

		}

		return updated;
	}

	public void updateUsersCache()
	{
		usersCache = (LinkedHashMap)users.clone();
		if(userUpdateCallback != null) {
			userUpdateCallback.run();
		}
	}

	public Iterable<? extends IClientUserInfo> getClientUserInfoIterator()
	{
		return usersCache.values();
	}
}

