package cs451.broadcast;

import java.io.IOException;

import cs451.message.BroadcastMessage;

public interface Broadcaster {

	public interface Receiver {
		public void deliver(BroadcastMessage message);
	}

	public void broadcast(BroadcastMessage message) throws IOException;
}
