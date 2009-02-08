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

import java.net.*;

public class UserInfo implements IUserInfo
{
	private String name;
	private SocketAddress address;
	private int timeout;

	UserInfo(SocketAddress a, String n)
	{
		name = n;
		address = a;
		timeout = 0;
	}

	public String getUserName()
	{
		return name;
	}

	public SocketAddress getSocketAddress()
	{
		return address;
	}

	public void setSocketAddress(SocketAddress a)
	{
		address = a;
	}

	public void userData(String n)
	{
		name = n;
		timeout = 0;
	}

	public void resetTimeout()
	{
		timeout = 0;
	}

	public int stepTimeout()
	{
		return ++timeout;
	}
}

