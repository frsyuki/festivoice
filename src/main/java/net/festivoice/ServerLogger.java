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
import java.sql.*;

public class ServerLogger extends Thread
{
	// singleton
	private static ServerLogger instance = new ServerLogger();

	public static ServerLogger getInstance()
	{
		return instance;
	}


	private PrintStream textStream = System.out;

	private ServerLogger()
	{
	}

	public void setTextStream(PrintStream stream)
	{
		textStream = stream;
	}

	private Connection connection = null;

	private GregorianCalendar calendar;

	private PreparedStatement channelCreateStatement;
	private PreparedStatement channelJoinStatement;
	private PreparedStatement channelLeaveStatement;
	private PreparedStatement channelRemoveStatement;
	private PreparedStatement channelEventsStatement;

	public class ChannelEventType {
		public static final int CREATE = 1;
		public static final int JOIN   = 2;
		public static final int LEAVE  = 3;
		public static final int REMOVE = 4;
	}

	public void enableDatabase(String path) throws IOException
	{
		// JDBCを有効化する
		// デフォルトはテキストのみ
		File file = new File(path);
		boolean fileExist = file.exists();

		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

			String url = "jdbc:derby:" + path;
			if(!fileExist) {
				url += ";create=true";
			} else {
				url += ";create=false";
			}

			connection = DriverManager.getConnection(url);

			if(!fileExist) {
				createTables();
			}

			createStatements();

			calendar = new GregorianCalendar();

		} catch(Exception e) {
			e.printStackTrace();
			throw new IOException("can't initialize database: " + e);
		}
	}

	private void createTables() throws SQLException
	{
		connection.createStatement().executeUpdate(
				"create table \"channelEvent\" (\"time\" TIMESTAMP, \"type\" INTEGER, \"channel\" VARCHAR(256), \"user\" VARCHAR(256), \"address\" VARCHAR(64), \"port\" INTEGER)");
	}

	private void createStatements() throws SQLException
	{
		channelCreateStatement = connection.prepareStatement(
				"insert into \"channelEvent\" (\"time\", \"type\", \"channel\", \"user\", \"address\", \"port\") VALUES (?, "+ChannelEventType.CREATE+", ?, null, null, null)");
		channelJoinStatement   = connection.prepareStatement(
				"insert into \"channelEvent\" (\"time\", \"type\", \"channel\", \"user\", \"address\", \"port\") VALUES (?, "+ChannelEventType.JOIN+", ?, ?, ?, ?)");
		channelLeaveStatement  = connection.prepareStatement(
				"insert into \"channelEvent\" (\"time\", \"type\", \"channel\", \"user\", \"address\", \"port\") VALUES (?, "+ChannelEventType.LEAVE+", ?, ?, ?, ?)");
		channelRemoveStatement = connection.prepareStatement(
				"insert into \"channelEvent\" (\"time\", \"type\", \"channel\", \"user\", \"address\", \"port\") VALUES (?, "+ChannelEventType.REMOVE+", ?, null, null, null)");
		channelEventsStatement = connection.prepareStatement(
				"select \"time\", \"type\", \"channel\", \"user\", \"address\", \"port\" from \"channelEvent\" order by \"time\" desc");
	}


	public static Timestamp getCurrentTime()
	{
		return new Timestamp(System.currentTimeMillis());
	}

	public void channelCreated(String channelName)
	{
		textStream.println("create: '"+channelName+"'");
		if(connection == null) { return; }

		try {
			channelCreateStatement.setTimestamp(1, getCurrentTime());
			channelCreateStatement.setString(2, channelName);
			channelCreateStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void channelJoined(String channelName, String userName, InetSocketAddress userAddress)
	{
		textStream.println("join: "+userAddress+" '"+channelName+"' <- '"+userName+"'");
		if(connection == null) { return; }

		try {
			channelJoinStatement.setTimestamp(1, getCurrentTime());
			channelJoinStatement.setString(2, channelName);
			channelJoinStatement.setString(3, userName);
			channelJoinStatement.setString(4, userAddress.getAddress().toString());
			channelJoinStatement.setInt(5, userAddress.getPort());
			channelJoinStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void channelLeaved(String channelName, String userName, InetSocketAddress userAddress)
	{
		textStream.println("leave: "+userAddress+" '"+channelName+"' -> '"+userName+"'");
		if(connection == null) { return; }

		try {
			channelLeaveStatement.setTimestamp(1, getCurrentTime());
			channelLeaveStatement.setString(2, channelName);
			channelLeaveStatement.setString(3, userName);
			channelLeaveStatement.setString(4, userAddress.getAddress().toString());
			channelLeaveStatement.setInt(5, userAddress.getPort());
			channelLeaveStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void channelRemoved(String channelName)
	{
		textStream.println("remove: '"+channelName+"'");
		if(connection == null) { return; }

		try {
			channelRemoveStatement.setTimestamp(1, getCurrentTime());
			channelRemoveStatement.setString(2, channelName);
			channelRemoveStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public class Event {
		private Timestamp time;
		private int type;
		private String channel;
		private String address;
		private int port;
		private String user;

		Event(Timestamp time, int type, String channel, String user, String address, int port)
		{
			this.time = time;
			this.type = type;
			this.channel = channel;
			this.address = address;
			this.port = port;
			this.user = user;
		}

		public Timestamp getTimestamp()
		{
			return time;
		}

		public int getType()
		{
			return type;
		}

		public String getChannelName()
		{
			return channel;
		}

		public String getAddress()
		{
			return address;
		}

		public int getPort()
		{
			return port;
		}

		public String getUserName()
		{
			return user;
		}

	}

	public Iterable<? extends Event> getEvent(int n)
	{
		ArrayList<Event> ret = new ArrayList<Event>();
		if(connection == null) { return ret; }

		try {
			channelEventsStatement.setMaxRows(n);
			ResultSet result = channelEventsStatement.executeQuery();
			while(result.next()) {
				ret.add( new Event(
							result.getTimestamp(1),
							result.getInt(2),
							result.getString(3),
							result.getString(4),
							result.getString(5),
							result.getInt(6)
							) );
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ret;
	}
}

