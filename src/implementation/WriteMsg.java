package implementation;

public class WriteMsg {

	private long transactionId;
	private  long timeStamp;
	private ReplicaLoc loc;

	public long getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(long transactionId) {
		this.transactionId = transactionId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public ReplicaLoc getLoc() {
		return loc;
	}

	public void setLoc(ReplicaLoc loc) {
		this.loc = loc;
	}
}