package implementation;

import interfaces.ReplicaServerClientInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

public class ReplicaServer implements ReplicaServerClientInterface {

    @Override
    public AckMsg write(long txnID, long msgSeqNum, FileContent data) throws RemoteException, IOException {
        return null;
    }

    @Override
    public FileContent read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        return null;
    }

    @Override
    public boolean commit(long txnID, long numOfMsgs) throws MessageNotFoundException, RemoteException {
        return false;
    }

    @Override
    public boolean abort(long txnID) throws RemoteException {
        return false;
    }
}
