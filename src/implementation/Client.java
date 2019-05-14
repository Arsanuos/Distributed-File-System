package implementation;

import interfaces.MasterServerClientInterface;
import interfaces.ReplicaServerClientInterface;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class Client {

    private MasterServerClientInterface master;

    public Client(String addr, int port){
        try {
            // Getting the registry
            Registry registry = LocateRegistry.getRegistry(addr, port);

            // Looking up the registry for the remote object
            this.master = (MasterServerClientInterface) registry.lookup("Master");

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public byte[] read(String fileName) throws IOException, NotBoundException {
        ReplicaLoc[] locations = master.read(fileName);

        // get replication.
        ReplicaLoc replica = locations[0];

        //get the server itself.
        Registry registry = LocateRegistry.getRegistry(replica.getAddress());
        ReplicaServerClientInterface replicaServer = (ReplicaServerClientInterface) registry.lookup("Replica" + replica.getId());

        FileContent fileContent = replicaServer.read(fileName);

        return fileContent.getData();
    }


    public void write(FileContent fileContent) throws IOException, NotBoundException, MessageNotFoundException {
        WriteMsg msg = master.write(fileContent);
        ReplicaLoc replicaLoc = msg.getLoc();

        //get the server itself.
        Registry registry = LocateRegistry.getRegistry(replicaLoc.getAddress());
        ReplicaServerClientInterface replicaServer = (ReplicaServerClientInterface) registry.lookup("Replica" + replicaLoc.getId());

        byte[] data = fileContent.getData();
        int len = (int) Math.floor(1.0 * fileContent.getData().length/Configuration.CHUNK_SIZE);
        FileContent sent = new FileContent();

        for(int i = 0 ;i < len; i++){
            byte[] chunk = Arrays.copyOfRange(data, i * Configuration.CHUNK_SIZE, (i + 1) * Configuration.CHUNK_SIZE);
            sent.setData(chunk);
            replicaServer.write(msg.getTransactionId(), i + 1, sent);
        }

        if(fileContent.getData().length % Configuration.CHUNK_SIZE != 0){
            byte[] chunk = Arrays.copyOfRange(data, len * Configuration.CHUNK_SIZE,fileContent.getData().length);
            sent.setData(chunk);
            replicaServer.write(msg.getTransactionId(), len + 1, sent);
        }
        replicaServer.commit(msg.getTransactionId(), len + 1);
    }


}
