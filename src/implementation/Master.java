package implementation;

import interfaces.MasterClientInterface;
import interfaces.ReplicaMasterInterface;
import utils.Configuration;
import utils.FileContent;
import utils.WriteMsg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Master implements MasterClientInterface {


    private Map<String, ReplicaLoc> primaryReplica;
    private Map<String, List<ReplicaLoc>> replicaLocations;
    private List<ReplicaLoc> replicaLocs;
    private List<Replica> replicaServers;
    private long txID;
    private Registry registry;




    public Master(List<Replica> replicaServers, List<ReplicaLoc> replicaLocs){

        this.replicaLocs = replicaLocs;
        this.replicaServers = replicaServers;
        primaryReplica = new HashMap<>();
        replicaLocations = new HashMap<>();
        this.txID = 0;

        TimerTask heartBeat = new TimerTask() {
            @Override
            public void run() {
                for(int i = 0 ;i < replicaServers.size(); i++){
                    try {
                        Replica server = replicaServers.get(i);
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

        try {
            registry = LocateRegistry.getRegistry(Configuration.REG_PORT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public ReplicaLoc read(String fileName) throws IOException, RemoteException {
        if(!this.replicaLocations.containsKey(fileName)){
            throw new FileNotFoundException();
        }
        List<ReplicaLoc> locs = this.replicaLocations.get(fileName);
        ReplicaLoc[] locations = locs.toArray(new ReplicaLoc[locs.size()]);
        return locations[0];
    }

    @Override
    public WriteMsg write(FileContent data) throws RemoteException, IOException, NotBoundException {
        if(replicaLocations.containsKey(data.getFileName())){
            // fill already handled by primary replica
            List<ReplicaLoc> locs = this.replicaLocations.get(data.getFileName());
            ReplicaLoc primary = locs.get(0);
            long txnID = txID++;
            long timeStamp = txnID;
            return new WriteMsg(txnID, timeStamp, primary);
        }

        // sample from ReplicLoc
        List<ReplicaLoc> sampled_loc = sample_locs(replicaLocs);
        ReplicaLoc prime = sampled_loc.get(0);
        ReplicaMasterInterface inter = (ReplicaMasterInterface) registry.lookup("Replica"+prime.getId());
        inter.take_charge(data.getFileName(), sampled_loc);
        replicaLocations.put(data.getFileName(), sampled_loc);
        return write(data);
    }

    private List<ReplicaLoc> sample_locs(List<ReplicaLoc> total_locs){
        if(total_locs.size() <= Configuration.NUM_REPLICA){
            return total_locs;
        }
        int sz = total_locs.size();
        Random random = new Random();
        ReplicaLoc primary = total_locs.get(random.nextInt(sz));
        List<ReplicaLoc> sampled_locs = new ArrayList<>(Configuration.NUM_REPLICA);
        sampled_locs.add(0, primary);

        for(int i = 1 ; i < Configuration.NUM_REPLICA; i++){
            ReplicaLoc replica = total_locs.get(random.nextInt(sz));
            if(replica.getId() == primary.getId()){
                // redo the loop again
                i--;
            }else {
                sampled_locs.add(i, replica);
            }
        }
        return sampled_locs;
    }
}
