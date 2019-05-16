package interfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReplicaReplicaInterface extends Remote {

    public boolean reflect_data(String filename, byte[] data, long txnID) throws IOException, RemoteException;

    public boolean release_file_locks(String filename, long txnID) throws RemoteException;


}
