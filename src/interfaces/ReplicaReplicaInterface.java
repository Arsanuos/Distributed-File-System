package interfaces;

import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

public interface ReplicaReplicaInterface extends Remote {

    public boolean commit_data(String filename, byte[] data) throws IOException;

    public boolean release_locks(String filename);


}
