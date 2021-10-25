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
import java.util.concurrent.atomic.AtomicBoolean;

public final class PerfectLink {
	private final static int MAX_PAYLOAD_LENGTH = 1000;
	private final static long ACK_DELAY = 100;

	private final DatagramSocket socket;
	private final AtomicBoolean running;
	private final ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>> unacked;
	private final Receiver receiver;

	public PerfectLink(Receiver receiver) throws SocketException, UnknownHostException {
		Host curHost = HostInfo.getHost(HostInfo.getCurrentHostId());
		this.receiver = receiver;
		this.socket = new DatagramSocket(curHost.getPort(), InetAddress.getByName(curHost.getIp()));
		this.unacked = new ConcurrentHashMap<Integer, ConcurrentSkipListSet<Integer>>();
		this.running = new AtomicBoolean(true);
		new Thread(packetHandler()).start();
	}

	public void stop() {
		running.set(false);
	}

	private Runnable packetHandler() {
		return new Runnable() {

			public void run() {
				Map<Integer, Set<Integer>> delivered = new HashMap<Integer, Set<Integer>>();

				for (int i = 1; i <= HostInfo.numHosts(); i++) {
					delivered.put(i, new HashSet<Integer>());
				}
				while (running.get()) {

					byte[] payload = new byte[MAX_PAYLOAD_LENGTH];
					DatagramPacket packet = new DatagramPacket(payload, MAX_PAYLOAD_LENGTH);
					try {
						socket.receive(packet);

						String ip = Host.ipFromInetAddress(packet.getAddress());
						int senderId = HostInfo.hostIdfromIpAndPort(ip, packet.getPort());
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
							socket.send(ackPacket);

							if (!delivered.get(senderId).contains(message.getSequenceNbr())) {
								delivered.get(senderId).add(message.getSequenceNbr());
								receiver.deliver(message, senderId);
							}
						}
					} catch (UnknownHostException e1) {
						e1.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		};

	}

	public void send(Message message, int destHost) throws IOException {
		DatagramPacket packet = message.toPacket();
		Host destination = HostInfo.getHost(destHost);

		packet.setAddress(InetAddress.getByName(destination.getIp()));
		packet.setPort(destination.getPort());
		socket.send(packet);

		ConcurrentSkipListSet<Integer> destUnacked = unacked.computeIfAbsent(destHost,
				hostId -> new ConcurrentSkipListSet<Integer>());
		destUnacked.add(message.getSequenceNbr());

		Timer ackChecker = new Timer();
		ackChecker.schedule(new TimerTask() {

			public void run() {
				if (destUnacked.contains(message.getSequenceNbr()) && running.get()) {
					try {
						send(message, destHost);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, ACK_DELAY);

	}

}