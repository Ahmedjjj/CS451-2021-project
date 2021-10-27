package cs451.link;

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
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.host.Host;
import cs451.host.HostInfo;
import cs451.message.P2PMessage;

public final class PerfectLink {
	public interface Receiver{
		public void deliver(P2PMessage message);
	}
	
	private final static int MAX_PAYLOAD_LENGTH = 1000;
	private final static long ACK_DELAY = 100;

	private final DatagramSocket socket;
	private final AtomicBoolean running;
	private final Set<P2PMessage> unacked;
	private final PerfectLink.Receiver receiver;

	public PerfectLink(PerfectLink.Receiver receiver) throws SocketException, UnknownHostException {
		Host curHost = HostInfo.getHost(HostInfo.getCurrentHostId());
		this.receiver = receiver;
		this.socket = new DatagramSocket(curHost.getPort(), curHost.getInetAddress());
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
				Set<P2PMessage> delivered = new HashSet<>();

				while (running.get()) {

					byte[] payload = new byte[MAX_PAYLOAD_LENGTH];
					DatagramPacket packet = new DatagramPacket(payload, MAX_PAYLOAD_LENGTH);

					try {
						socket.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}

					P2PMessage message = P2PMessage.fromPacket(packet);

					if (message.isAck()) {
						unacked.remove(message);
					} else {
						P2PMessage ackMsg = message.getAck();
						try {
							DatagramPacket ackPacket = ackMsg.toPacket();
							socket.send(ackPacket);
						} catch (UnknownHostException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}

						if (!delivered.contains(message)) {
							delivered.add(message);
							receiver.deliver(message);
						}
					}

				}
			}
		};

	}

	public void send(P2PMessage message) throws IOException {

		DatagramPacket packet = message.toPacket();
		socket.send(packet);

		P2PMessage ackMsg = message.getAck();
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