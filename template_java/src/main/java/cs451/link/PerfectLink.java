package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PerfectLink {
	private final static int MAX_PAYLOAD_LENGTH = 1000;
	private final static long ACK_DELAY = 100;

	private final DatagramSocket socket;
	private final AtomicBoolean running;
	private final Set<Message> unacked;
	private final Receiver receiver;

	public PerfectLink(Receiver receiver) throws SocketException, UnknownHostException {
		Host curHost = HostInfo.getHost(HostInfo.getCurrentHostId());
		this.receiver = receiver;
		this.socket = new DatagramSocket(curHost.getPort(),curHost.getInetAddress());
		this.unacked = ConcurrentHashMap.newKeySet();
		this.running = new AtomicBoolean(true);
		new Thread(packetHandler()).start();
	}

	public void stop() {
		running.set(false);
	}

	private Runnable packetHandler() {
		return new Runnable() {

			@Override
			public void run() {
				Set<Message> delivered = new HashSet<>();

				while (running.get()) {

					byte[] payload = new byte[MAX_PAYLOAD_LENGTH];
					DatagramPacket packet = new DatagramPacket(payload, MAX_PAYLOAD_LENGTH);
					try {
						socket.receive(packet);

						Message message = Message.fromPacket(packet);
						int senderId = message.getSenderId();
						Host senderHost = HostInfo.getHost(senderId);

						if (message.isAck()) {
							unacked.remove(message);
						} else {
							Message ackMsg = message.getAck();
							DatagramPacket ackPacket = ackMsg.toPacket();

							ackPacket.setPort(senderHost.getPort());
							ackPacket.setAddress(senderHost.getInetAddress());
							socket.send(ackPacket);

							if (!delivered.contains(message)) {
								delivered.add(message);
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

	public void send(Message message) throws IOException {

		DatagramPacket packet = message.toPacket();
		Host destinationHost = HostInfo.getHost(message.getReceiverId());
		packet.setAddress(destinationHost.getInetAddress());
		packet.setPort(destinationHost.getPort());
		socket.send(packet);

		Message ackMsg = message.getAck();
		unacked.add(ackMsg);
		Timer ackChecker = new Timer();
		ackChecker.schedule(new TimerTask() {

			@Override
			public void run() {
				if (unacked.contains(ackMsg) && running.get()) {
					try {
						send(message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}, ACK_DELAY);

	}

}