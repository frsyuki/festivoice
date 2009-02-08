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

import javax.xml.transform.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;

public class WebStatusView extends HttpServlet
{
	private String contentType;
	private String styleResourcePath;
	private IChannelManager channelManager;

	public void init() throws ServletException
	{
		ServletConfig config = getServletConfig();

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
		Document doc;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch(Exception e) {
			throw new IOException("failed to create XML document: "+e);
		}

		Element channels = doc.createElement("channels");
		for(IChannelInfo channelInfo : WebServerLoader.getChannelManager().getChannels()) {
			if(channelInfo.getChannelName() == null) {
				System.out.println("null channel name");
				continue;
			}

			Element channel = doc.createElement("channel");

			Element cname = doc.createElement("name");
			cname.appendChild(doc.createTextNode( channelInfo.getChannelName() ));

			Element users = doc.createElement("users");
			for(IUserInfo userInfo : channelInfo.getUsers()) {
				if(userInfo.getUserName() == null) {
					System.out.println("null user name");
					continue;
				}

				Element user = doc.createElement("user");

				Element uaddr = doc.createElement("address");
				uaddr.appendChild(doc.createTextNode( userInfo.getSocketAddress().toString() ));

				Element uname = doc.createElement("name");
				uname.appendChild(doc.createTextNode( userInfo.getUserName() ));

				user.appendChild(uname);
				user.appendChild(uaddr);
				users.appendChild(user);
			}

			channel.appendChild(cname);
			channel.appendChild(users);
			channels.appendChild(channel);
		}
		doc.appendChild(channels);

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

