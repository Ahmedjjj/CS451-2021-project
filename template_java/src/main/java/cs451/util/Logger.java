package cs451.util;

import java.io.FileWriter;
import java.io.IOException;

import cs451.message.Message;

public final class Logger implements Receiver {

	private final StringBuilder log = new StringBuilder();

	@Override
	public void deliver(Message message, int senderId) {
		logDeliver(message.getSequenceNbr(), senderId);
	}

	public void logDeliver(int sequenceNbr, int senderId) {
		log.append(String.format("d %d %d\n", senderId, sequenceNbr));
	}

	public void logBroadcast(int sequenceNbr) {
		log.append(String.format("b %d\n", sequenceNbr));

	}

	public void writeToFile(String path) throws IOException {
		FileWriter writer = new FileWriter(path);
		writer.write(log.toString());
		writer.close();
	}
}
