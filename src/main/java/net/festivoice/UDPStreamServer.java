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
import java.util.concurrent.*;

class UDPDataWithAddress extends UDPData
{
	private SocketAddress address;

	public UDPDataWithAddress(DatagramPacket received) throws Exception
	{
		super();
		address = received.getSocketAddress();
		fromByteArray(received.getData());
	}

	public SocketAddress getSocketAddress()
	{
		return address;
	}
}

class Worker extends Thread
{
	private DatagramSocket socket;
	private IChannelManager channelServer;
	private BlockingQueue<UDPDataWithAddress> queue;
	private boolean endFlag = false;

	public Worker(DatagramSocket socket, IChannelManager channelServer, BlockingQueue<UDPDataWithAddress> queue)
	{
		this.socket = socket;
		this.channelServer = channelServer;
		this.queue = queue;
	}

	public void run()
	{
		while(!endFlag) {
			try {
				UDPDataWithAddress data = queue.take();
				SocketAddress fromAddress = data.getSocketAddress();

				IChannelInfo channel = channelServer.channelData(data.getChannelName(),
						data.getUserName(), fromAddress);

				short userIndex = 0;
				for(IUserInfo user : channel.getUsers()) {
					if(user.getSocketAddress().equals(fromAddress)) {
						break;
					} else {
						++userIndex;
					}
				}

				for(IUserInfo user : channel.getUsers()) {
					if(!user.getSocketAddress().equals(fromAddress)) {
						data.setUserIndex(userIndex);
						byte[] send_data = data.toByteArray();
						DatagramPacket send = new DatagramPacket(send_data, send_data.length, user.getSocketAddress());
						socket.send(send);
					}
				}
			} catch (Exception e) {
				//System.out.println("send error: "+e);
				//e.printStackTrace();
			}
		}
	}

	public void end()
	{
		endFlag = true;
	}
}

public class UDPStreamServer extends AbstractStreamServer {
	private DatagramSocket socket;
	private Worker[] workers;
	private BlockingQueue<UDPDataWithAddress> queue;

	UDPStreamServer(SocketAddress addr, IChannelManager channelServer) throws SocketException
	{
		socket = new DatagramSocket(addr);

		queue = new LinkedBlockingQueue<UDPDataWithAddress>(1024);  // FIXME

		workers = new Worker[1];  // FIXME
		for(int i=0; i < workers.length; ++i) {
			workers[i] = new Worker(socket, channelServer, queue);
		}
	}

	public void run()
	{
		for(Worker w : workers) {
			w.start();
		}
		byte[] buf = new byte[1024 * 35];
		DatagramPacket received = new DatagramPacket(buf, buf.length);
		received.setLength(buf.length);
		while (true) {
			try {
				socket.receive(received);

				UDPDataWithAddress data = new UDPDataWithAddress(received);
				queue.put(data);

			} catch (Exception e) {
			}
		}
	}
}

