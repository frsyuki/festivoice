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
import java.text.DateFormat;
import java.sql.Timestamp;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.transform.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

public class WebStatusView extends HttpServlet
{
	private String contentType;
	private String styleResourcePath;
	private IChannelManager channelManager;
	private int eventLimit;
	private int newThreshold;

	public void init() throws ServletException
	{
		ServletConfig config = getServletConfig();

		String eventLimitString = config.getInitParameter("eventLimit");
		if(eventLimitString == null) {
			eventLimitString = "20";
		}
		eventLimit = Integer.parseInt(eventLimitString);

		String newThresholdString = config.getInitParameter("newThreshold");
		if(newThresholdString == null) {
			newThresholdString = "8640";
		}
		newThreshold = Integer.parseInt(newThresholdString);

		contentType = config.getInitParameter("contentType");
		if(contentType == null) {
			contentType = "text/html; charset=UTF-8";
		}

		styleResourcePath = config.getInitParameter("style");
		if(styleResourcePath == null) {
			styleResourcePath = "/index.xsl";
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		//req.setCharacterEncoding("UTF-8");
		//res.setCharacterEncoding("UTF-8");
		res.setContentType(contentType);
		Document doc = xmlChannelInfo();
		applyStyle(
				new javax.xml.transform.dom.DOMSource(doc),
				new javax.xml.transform.stream.StreamResult(res.getWriter()));
	}

	private Document xmlChannelInfo() throws IOException
	{
		Timestamp currentTime = ServerLogger.getCurrentTime();

		Document doc;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch(Exception e) {
			throw new IOException("failed to create XML document: "+e);
		}

		Element eStatus = doc.createElement("status");

		// channels
		Element eChannels = doc.createElement("channels");
		for(IChannelInfo channelInfo : WebServerLoader.getChannelManager().getChannels()) {
			if(channelInfo.getChannelName() == null) {
				System.out.println("null channel name");
				continue;
			}

			String channelName = channelInfo.getChannelName();
			boolean hidden = channelName.startsWith("_");

			Element eChannel = doc.createElement("channel");

			Element eChannelName = doc.createElement("cname");
			eChannelName.appendChild(doc.createTextNode( channelName ));
			eChannel.appendChild(eChannelName);

			Element eChannelURL = doc.createElement("cnameURL");
			eChannelURL.appendChild(doc.createTextNode( URLEncoder.encode(channelName, "UTF-8") ));
			eChannel.appendChild(eChannelURL);

			Element eHidden = doc.createElement("hidden");
			eHidden.appendChild(doc.createTextNode( hidden ? "1" : "0"  ));
			eChannel.appendChild(eHidden);

			Element eUsers = doc.createElement("users");
			for(IUserInfo userInfo : channelInfo.getUsers()) {
				if(userInfo.getUserName() == null) {
					System.out.println("null user name");
					continue;
				}

				String userName                 = userInfo.getUserName();
				InetSocketAddress socketAddress = userInfo.getInetSocketAddress();
				InetAddress address             = socketAddress.getAddress();
				int port                        = socketAddress.getPort();

				Element eUser = doc.createElement("user");

				Element eAddress = doc.createElement("address");
				eAddress.appendChild(doc.createTextNode( address.toString() ));

				Element ePort = doc.createElement("port");
				ePort.appendChild(doc.createTextNode( Integer.toString(port) ));

				Element eName = doc.createElement("name");
				eName.appendChild(doc.createTextNode( userName ));

				eUser.appendChild(eName);
				eUser.appendChild(eAddress);
				eUsers.appendChild(eUser);
			}
			eChannel.appendChild(eUsers);

			eChannels.appendChild(eChannel);
		}
		eStatus.appendChild(eChannels);

		// events
		Element eEvents = doc.createElement("events");
		for(ServerLogger.Event loggerEvent : ServerLogger.getInstance().getEvent(eventLimit)) {

			Timestamp time      = loggerEvent.getTimestamp();
			DateFormat format   = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
			String timeString   = format.format(time);
			int type            = loggerEvent.getType();
			String channelName  = loggerEvent.getChannelName();
			String userName     = loggerEvent.getUserName();
			String address      = loggerEvent.getAddress();
			int port            = loggerEvent.getPort();
			boolean hidden      = channelName.startsWith("_");
			boolean newone      = time.getTime() - currentTime.getTime() < newThreshold;

			Element eEvent = doc.createElement("event");

			Element eTime = doc.createElement("time");
			eTime.appendChild(doc.createTextNode( timeString ));
			eEvent.appendChild(eTime);

			Element eType = doc.createElement("type");
			eType.appendChild(doc.createTextNode( Integer.toString(type) ));
			eEvent.appendChild(eType);

			Element eHidden = doc.createElement("hidden");
			eHidden.appendChild(doc.createTextNode( hidden ? "1" : "0" ));
			eEvent.appendChild(eHidden);

			Element eNew = doc.createElement("new");
			eNew.appendChild(doc.createTextNode( newone ? "1" : "0" ));
			eEvent.appendChild(eNew);

			if(channelName != null) {
				Element eChannel = doc.createElement("channel");
				eChannel.appendChild(doc.createTextNode( channelName ));
				eEvent.appendChild(eChannel);

				Element eChannelURL = doc.createElement("channelURL");
				eChannelURL.appendChild(doc.createTextNode( URLEncoder.encode(channelName, "UTF-8") ));
				eEvent.appendChild(eChannelURL);
			}

			if(userName != null) {
				Element eName = doc.createElement("user");
				eName.appendChild(doc.createTextNode( loggerEvent.getUserName() ));
				eEvent.appendChild(eName);
			}

			if(address != null) {
				Element eAddress = doc.createElement("address");
				eAddress.appendChild(doc.createTextNode( loggerEvent.getAddress().toString() ));
				eEvent.appendChild(eAddress);
			}

			if(port != 0) {
				Element ePort = doc.createElement("port");
				ePort.appendChild(doc.createTextNode( Integer.toString(loggerEvent.getPort()) ));
				eEvent.appendChild(ePort);
			}

			eEvents.appendChild(eEvent);
		}
		eStatus.appendChild(eEvents);

		doc.appendChild(eStatus);

		return doc;
	}

	private void applyStyle(Source src, Result out) throws IOException
	{
		ServletConfig config = getServletConfig();
		ServletContext ctx = config.getServletContext();
		try {
			TransformerFactory factory = TransformerFactory.newInstance();
			InputStream xsl = ctx.getResourceAsStream(styleResourcePath);
			Transformer transformer = factory.newTransformer(
					new javax.xml.transform.stream.StreamSource(xsl));
			transformer.transform(src, out);
		} catch(Exception e) {
			e.printStackTrace();
			throw new IOException("failed to create apply XSL Transform: "+e);
		}
	}
}

