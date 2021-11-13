package cs451.util;

import java.io.FileWriter;
import java.io.IOException;

import cs451.message.BroadcastMessage;
import cs451.message.P2PMessage;
import cs451.broadcast.Broadcaster;
import cs451.link.PerfectLink;

public final class Logger implements PerfectLink.Receiver, Broadcaster.Receiver{

	private final StringBuilder log = new StringBuilder();

	public void logDeliver(int sequenceNbr, int senderId) {
		System.out.println(String.format("d %d %d\n", senderId, sequenceNbr));
		log.append(String.format("d %d %d\n", senderId, sequenceNbr));
	}

	public void logBroadcast(int sequenceNbr) {
		System.out.println(String.format("b %d\n", sequenceNbr));
		log.append(String.format("b %d\n", sequenceNbr));
	}

	public void writeToFile(String path) throws IOException {
		FileWriter writer = new FileWriter(path);
		writer.write(log.toString());
		writer.close();
	}

	@Override
	public void deliver(P2PMessage message) {
		logDeliver(message.getSequenceNbr(), message.getSenderId());
	}

	@Override
	public void deliver(BroadcastMessage message) {
		logDeliver(message.getOriginalSequenceNbr(), message.getOriginalSenderId());
	}
}
