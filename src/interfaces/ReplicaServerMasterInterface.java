package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicaServerMasterInterface extends Remote {

    public boolean isAlive() throws RemoteException;
}
