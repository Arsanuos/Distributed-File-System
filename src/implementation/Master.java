package implementation;

import interfaces.MasterServerClientInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

public class Master implements MasterServerClientInterface {

    @Override
    public ReplicaLoc[] read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        return new ReplicaLoc;
    }

    @Override
    public WriteMsg write(FileContent data) throws RemoteException, IOException {
        return null;
    }
}
