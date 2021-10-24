package cs451;

public interface Receiver {
	public void deliver(Message message, int senderId);
}
