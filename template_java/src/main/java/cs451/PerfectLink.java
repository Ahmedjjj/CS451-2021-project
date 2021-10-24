package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public final class PerfectLink {
	private final static int MAX_PAYLOAD_LENGTH = 1000;
	private final static long ACK_DELAY = 100;

	private final DatagramSocket socket;
	private final ConcurrentLinkedQueue<DatagramPacket> receivedPackets;
	private final ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> unacked;
	private final Receiver receiver;

	public PerfectLink(Receiver receiver) throws SocketException, UnknownHostException {
		Host curHost = HostInfo.getHost(HostInfo.getCurrentHostId());
		this.receiver = receiver;
		this.socket = new DatagramSocket(curHost.getPort(), InetAddress.getByName(curHost.getIp()));
		this.receivedPackets = new ConcurrentLinkedQueue<DatagramPacket>();
		this.unacked = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>>();
		this.packetReceiver().run();
		this.packetHandler().run();
	}

	private Runnable packetReceiver() {
		return () -> {
			while (true) {
				byte[] payload = new byte[MAX_PAYLOAD_LENGTH];
				DatagramPacket received = new DatagramPacket(payload, MAX_PAYLOAD_LENGTH);
				try {
					socket.receive(received);
				} catch (IOException e) {
					e.printStackTrace();
				}
				receivedPackets.add(received);
			}
		};
	}

	private Runnable packetHandler() {
		return new Runnable() {

			public void run() {
				Map<Integer, Set<Integer>> delivered = new HashMap<Integer, Set<Integer>>();

				for (int i = 0; i < HostInfo.numHosts(); i++) {
					delivered.put(i, new HashSet<Integer>());
				}
				while (true) {

					DatagramPacket packet = receivedPackets.poll();
					int senderId = HostInfo.hostIdfromIpAndPort(packet.getAddress().toString(), packet.getPort());

					if (packet != null) {
						Message message = Message.fromPacket(packet);
						if (message.isAck()) {
							ConcurrentSkipListSet<Integer> senderUnacked = unacked.computeIfAbsent(senderId,
									hostId -> new ConcurrentSkipListSet<Integer>());
							senderUnacked.remove(message.getSequenceNbr());
						} else {
							Message ackMsg = message.toAckMessage();
							DatagramPacket ackPacket = ackMsg.toPacket();
							ackPacket.setPort(packet.getPort());
							ackPacket.setAddress(packet.getAddress());
							try {
								socket.send(ackPacket);
							} catch (IOException e) {
								e.printStackTrace();
							}
							if (!delivered.get(senderId).contains(message.getSequenceNbr())) {
								delivered.get(senderId).add(message.getSequenceNbr());
								receiver.receive(message, senderId);
							}

						}
					}
				}
			}
		};

	}

	public void send(Message message, int destHost) {
		DatagramPacket packet = message.toPacket();
		Host destination = HostInfo.getHost(destHost);
		try {
			packet.setAddress(InetAddress.getByName(destination.getIp()));
			packet.setPort(destination.getPort());
			socket.send(packet);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ConcurrentSkipListSet<Integer> destUnacked = unacked.computeIfAbsent(destHost,
				hostId -> new ConcurrentSkipListSet<Integer>());
		destUnacked.add(message.getSequenceNbr());

		Timer ackChecker = new Timer();
		ackChecker.schedule(new TimerTask() {

			public void run() {
				if (destUnacked.contains(message.getSequenceNbr())) {
					send(message, destHost);
				}
			}
		}, ACK_DELAY);

	}

}