package cs451;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import cs451.host.Host;
import cs451.host.HostInfo;
import cs451.link.PerfectLink;
import cs451.message.Message;
import cs451.parser.Parser;
import cs451.util.Logger;

public class Main {

	private static final Logger logger = new Logger();
	private static String outputPath;
	private static PerfectLink link;

	private static void handleSignal() {
		// immediately stop network packet processing
		System.out.println("Immediately stopping network packet processing.");

		// write/flush output file if necessary
		System.out.println("Writing output.");
		try {
			logger.writeToFile(outputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		link.stop();
	}

	private static void initSignalHandlers() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				handleSignal();
			}
		});
	}

	private static int getNumMessages(String configPath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(configPath));
		String line = reader.readLine();
		reader.close();
		return Integer.parseInt(line.split("\\s+")[0]);
	}

	private static int getReceiverHost(String configPath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(configPath));
		String line = reader.readLine();
		reader.close();
		return Integer.parseInt(line.split("\\s+")[1]);

	}

	public static void main(String[] args) throws InterruptedException, IOException {
		Parser parser = new Parser(args);
		parser.parse();

		initSignalHandlers();

		// example
		long pid = ProcessHandle.current().pid();
		System.out.println("My PID: " + pid + "\n");
		System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid
				+ "` to stop processing packets\n");

		System.out.println("My ID: " + parser.myId() + "\n");
		System.out.println("List of resolved hosts is:");
		System.out.println("==========================");
		for (Host host : parser.hosts()) {
			System.out.println(host.getId());
			System.out.println("Human-readable IP: " + host.getIp());
			System.out.println("Human-readable Port: " + host.getPort());
			System.out.println();
		}
		System.out.println();

		System.out.println("Path to output:");
		System.out.println("===============");
		System.out.println(parser.output() + "\n");

		System.out.println("Path to config:");
		System.out.println("===============");
		System.out.println(parser.config() + "\n");

		System.out.println("Doing some initialization\n");
		outputPath = parser.output();
		HostInfo.configureFromHostList(parser.hosts());
		HostInfo.setCurrentHostId(parser.myId());
		link = new PerfectLink(logger);

		int receiverHost = getReceiverHost(parser.config());
		int numMessages = getNumMessages(parser.config());
		System.out.println("Broadcasting and delivering messages...\n");
		System.out.println("Receiver: " + receiverHost);
		System.out.println("Number of messages: " + numMessages);

		if (parser.myId() != receiverHost) {
			for (int i = 1; i <= numMessages; i++) {
				link.send(new Message(i, HostInfo.getCurrentHostId(), receiverHost));
				logger.logBroadcast(i);
			}
		}
		// After a process finishes broadcasting,
		// it waits forever for the delivery of messages.
		while (true) {
			// Sleep for 1 hour
			Thread.sleep(60 * 60 * 1000);
		}
	}
}
