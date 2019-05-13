package interfaces;

import implementation.ReplicaLoc;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReplicaServerMasterInterface extends Remote {

    public boolean isAlive() throws RemoteException;

    public boolean take_charge(String filename, List<ReplicaLoc> replicas) throws RemoteException, NotBoundException;
}
