package cs451.broadcast;

import java.io.IOException;

import cs451.message.BroadcastMessage;
import cs451.util.Stoppable;

public abstract class Broadcaster implements Stoppable {

	public interface Receiver {
		public void deliver(BroadcastMessage message) throws IOException;
	}

	public abstract void broadcast(BroadcastMessage message) throws IOException;
}
