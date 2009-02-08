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

public class CUILauncher
{
	public static void main(String[] args) throws Exception
	{
		final String host = args[0];
		final int port = Integer.parseInt(args[1]);
		final String channel = args[2];

		String utmp = null;
		if(args.length > 3 && !args[3].equals("") && !args[3].equals("-")) {
			utmp = args[3];
		} else {
			utmp = System.getProperty("user.name");
		}
		final String user = utmp;

		final int mode = 2;
		final int quality = 8;
		final boolean vbr = true;

		final Client client = new Client(
				channel, user, new InetSocketAddress(host, port),
				mode, quality, vbr);

		final Runnable userUpdateCallback =
			new Runnable() {
				public void run() {
					System.out.println("----");
					System.out.println(" - "+user);
					for(IClientUserInfo user : client.getClientUserInfoIterator()) {
						System.out.println(" - "+user.getUserName());
					}
				}
			};

		client.setUserUpdateCallback(userUpdateCallback);

		client.setInitCallback( new Runnable() {
				public void run() {
					System.out.println("connected");
					userUpdateCallback.run();
				}
			});

		client.run();
	}
}

