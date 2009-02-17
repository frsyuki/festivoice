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

public class WebStart extends HttpServlet
{
	private String contentType;
	private String styleResourcePath;

	public void init() throws ServletException
	{
		ServletConfig config = getServletConfig();

		contentType = config.getInitParameter("contentType");
		if(contentType == null) {
			contentType = "application/x-java-jnlp-file; charset=UTF-8";
		}

		styleResourcePath = config.getInitParameter("template");
		if(styleResourcePath == null) {
			styleResourcePath = "/jnlp.xsl";
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException
	{
		//req.setCharacterEncoding("UTF-8");
		//res.setCharacterEncoding("UTF-8");
		res.setContentType(contentType);
		Document doc = createXmlInfo(req);
		applyStyle(
				new javax.xml.transform.dom.DOMSource(doc),
				new javax.xml.transform.stream.StreamResult(res.getWriter()));
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

	private Document createXmlInfo(HttpServletRequest req) throws IOException
	{
		String channelName = req.getPathInfo();
		if(channelName == null) {
			throw new IOException("empty channel name");
		}

		if(channelName.startsWith("/")) {
			channelName = channelName.substring(1);
		}

		Document doc;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.newDocument();
		} catch(Exception e) {
			throw new IOException("failed to create XML document: "+e);
		}

		Element start = doc.createElement("start");

		Element channel = doc.createElement("channel");
		channel.appendChild(doc.createTextNode( channelName ));
		start.appendChild(channel);

		Element channelURL = doc.createElement("channelURL");
		channelURL.appendChild(doc.createTextNode( URLEncoder.encode(channelName, "iso-8859-1") ));  // FIXME
		start.appendChild(channelURL);

		doc.appendChild(start);

		return doc;
	}
}

