package cs451.message;

public abstract class Message {
	private final byte[] payload;
	private final int senderId;

	public Message(byte[] payload, int senderId) {
		this.payload = new byte[payload.length];
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
		this.senderId = senderId;
	}

	public int getSenderId() {
		return senderId;
	}

	public byte[] getPayload() {
		return payload.clone();
	}
}