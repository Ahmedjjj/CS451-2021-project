package cs451.link;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import cs451.host.Host;
import cs451.host.HostInfo;
import cs451.message.P2PMessage;
import cs451.util.Stoppable;

public final class PerfectLink implements Stoppable {
	public interface Receiver {
		public void deliver(P2PMessage message) throws IOException;
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

					assert message.getReceiverId() == HostInfo.getCurrentHostId();

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
							try {
								receiver.deliver(message);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							System.out.println(
									"Found duplicate P2P message: sender Id: " + message.getSenderId() + " receiverId: "
											+ message.getReceiverId() + " seq num: " + message.getSequenceNbr());
						}
					}

				}
			}
		};

	}

	public void send(P2PMessage message){

		assert message.getSenderId() == HostInfo.getCurrentHostId();

		Thread sender = new Thread() {
			@Override
			public void run() {

				try {

					DatagramPacket packet;
					packet = message.toPacket();
					P2PMessage ackMsg = message.getAck();
					unacked.add(ackMsg);
					do {
						socket.send(packet);
						Thread.sleep(ACK_DELAY);
					} while ((unacked.contains(ackMsg) && running.get()));

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		sender.start();
	}

}