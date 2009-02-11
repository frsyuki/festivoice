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
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class WebServerLoader extends HttpServlet
{
	private static IChannelManager channelManager;

	public void init() throws ServletException
	{
		ServletConfig config = getServletConfig();

		String path = config.getInitParameter("database");
		if(path != null) {
			try {
				ServerLogger.getInstance().enableDatabase(path);
			} catch (IOException e) {
				throw new ServletException(e);
			}
		}

		int stream_port = Integer.parseInt(System.getProperty("streamPort", "6900"));
		InetSocketAddress stream_bind = new InetSocketAddress(stream_port);

		int timeoutInterval = Integer.parseInt(System.getProperty("timeoutInterval", "1000"));
		int timeoutLimit = Integer.parseInt(System.getProperty("timeoutLimit", "2"));

		StepTimeoutChannelManager m = new StepTimeoutChannelManager(timeoutInterval, timeoutLimit);
		m.start();

		try {
			AbstractStreamServer srv = new UDPStreamServer(stream_bind, m);
			srv.start();
		} catch (SocketException e) {
			e.printStackTrace();
			throw new ServletException(e);
		}

		channelManager = m;
	}

	public static IChannelManager getChannelManager()
	{
		return channelManager;
	}
}

