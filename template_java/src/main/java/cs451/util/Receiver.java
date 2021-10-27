package cs451.util;

import cs451.message.Message;

public interface Receiver {
	public void deliver(Message message, int senderId);
}
