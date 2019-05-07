package implementation;

import interfaces.MasterServerClientInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class Master implements MasterServerClientInterface {


    private Map<String, ReplicaLoc> primaryReplica;
    private Map<String, List<ReplicaLoc>> replicaLocations;
    private List<ReplicaLoc> replicaLocs;
    private List<ReplicaServer> replicaServers;

    public Master(List<ReplicaServer> replicaServers, List<ReplicaLoc> replicaLocs){

        this.replicaLocs = replicaLocs;
        this.replicaServers = replicaServers;
        primaryReplica = new HashMap<>();
        replicaLocations = new HashMap<>();

        TimerTask heartBeat = new TimerTask() {
            @Override
            public void run() {
                for(int i = 0 ;i < replicaServers.size(); i++){
                    try {
                        ReplicaServer server = replicaServers.get(i);
                        server.isAlive();
                    } catch (RemoteException e) {
                        replicaLocs.get(i).setAlive(false);
                        e.printStackTrace();
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(heartBeat, 10, 3000);
    }

    @Override
    public ReplicaLoc[] read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        List<ReplicaLoc> locs = this.replicaLocations.get(fileName);
        ReplicaLoc[] locations = locs.toArray(new ReplicaLoc[locs.size()]);
        return locations;
    }

    @Override
    public WriteMsg write(FileContent data) throws RemoteException, IOException {
        return null;
    }
}
