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

import javax.swing.*;
import java.awt.*;

public class AppletLauncher extends JApplet
{
	public void init()
	{
		String host = getParameter("host");
		int port = Integer.parseInt(getParameter("port"));

		String channel = getParameter("channel");
		if(channel == null || channel.equals("-")) {
			channel = "";
		}

		String user = getParameter("user");
		if(user == null || user.equals("-")) {
			try {
				user = System.getProperty("user.name");
			} catch (Exception e) {
				user = "";
			}
		}

		GUILauncher launcher = new GUILauncher(host, port, channel, user);
		getContentPane().add(launcher.getContentPane().getComponent(0));
	}
}

