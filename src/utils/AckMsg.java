package utils;

import java.io.Serializable;

public class AckMsg implements Serializable {

    private long transactionId;
    private long seqNo;


    public AckMsg(long tid, long seqNo) {
        this.transactionId = tid;
        this.seqNo = seqNo;
    }

    public long getSeqNo(){
        return seqNo;
    }

    public long getTxnID() {
        return transactionId;
    }
}
