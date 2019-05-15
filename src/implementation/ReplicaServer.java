package implementation;

import interfaces.ReplicaReplicaInterface;
import interfaces.ReplicaServerClientInterface;
import interfaces.ReplicaServerMasterInterface;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReplicaServer implements ReplicaServerClientInterface, ReplicaServerMasterInterface,
        ReplicaReplicaInterface {

    private int id;
    private String dir;
    private String addr;
    private Registry registry;
    private Map<Long, Map<Long, FileContent>> uncommittedFile;

    // this map is used to map from filename to slaves replicas
    private Map<String, List<ReplicaReplicaInterface>> slavesReplicas;
    private Map<Integer, ReplicaReplicaInterface> current_stubs;
    private Map<Long, String> txn_filename;
    private ConcurrentMap<String, ReentrantReadWriteLock> lock_manager;

    public ReplicaServer(int id, String addr){
        this.id = id;
        this.dir = "./replica-" + id;
        this.addr = addr;
        this.uncommittedFile = new HashMap<>();
        File file = new File(this.dir);
        if(!file.exists()){
            file.mkdir();
        }

        txn_filename = new HashMap<>();
        slavesReplicas = new HashMap<>();
        current_stubs = new HashMap<>();
        lock_manager = new ConcurrentHashMap<>();

        try {
            registry = LocateRegistry.getRegistry(addr, Configuration.REG_PORT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



    @Override
    public WriteMsg write(long txnID, long msgSeqNum, FileContent data) throws RemoteException, IOException {

        if(uncommittedFile.containsKey(txnID)){
            uncommittedFile.get(txnID).put(msgSeqNum, data);
        }else{
            Map<Long, FileContent> tmp = new HashMap<>();
            tmp.put(msgSeqNum, data);
            uncommittedFile.put(txnID, tmp);
            txn_filename.put(txnID, data.getFileName());
        }
        return new WriteMsg(txnID, msgSeqNum, new ReplicaLoc(this.id, this.addr, true));
    }

    @Override
    public FileContent read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        // in case of read the primary replica doesn't need to ask the slaves, but return its own copy
        lock_manager.putIfAbsent(fileName, new ReentrantReadWriteLock());
        ReentrantReadWriteLock lock = lock_manager.get(fileName);

        lock.readLock().lock();
        byte[] content = FileHandler.read(dir + File.separator + fileName);
        lock.readLock().unlock();

        return new FileContent(fileName, content);
    }

    private byte[] intoBytes(List<Map.Entry<Long, FileContent>> sortedList){
        int total_len = 0;
        for(int i = 0 ; i < sortedList.size(); i++){
            total_len += sortedList.get(i).getValue().getData().length;
        }
        byte[] arr = new byte[total_len];
        int curr = 0;
        for(int i = 0 ; i < sortedList.size(); i++){

            byte[] data = sortedList.get(i).getValue().getData();
            for(int j = 0 ; j < data.length; j++){
                arr[curr] = data[j];
                curr++;
            }
        }
        return arr;
    }

    @Override
    public boolean commit(long txnID, long numOfMsgs) throws MessageNotFoundException, IOException {
        // client calls it to end the transaction

        if(!this.uncommittedFile.containsKey(txnID)){
            throw new MessageNotFoundException();
        }


        Map<Long, FileContent> tmp = this.uncommittedFile.get(txnID);
        List<Map.Entry<Long, FileContent>> sortedList = getSortedActions(tmp);

        byte[] data = intoBytes(sortedList);

        // talk to the slaves to tell them to write first
        String filename = txn_filename.get(txnID);
        List<ReplicaReplicaInterface> slaves = slavesReplicas.get(filename);

        for (ReplicaReplicaInterface slave: slaves){
            boolean done = slave.reflect_data(filename, data);
            if (!done){
                // error, not handled yet
            }
        }
        boolean primary_done = reflect_data(filename, data);

        // unlock primary
        release_file_locks(filename);

        // unlock slaves
        for(ReplicaReplicaInterface slave: slaves){
            slave.release_file_locks(filename);
        }
        return true;
    }

    @Override
    public boolean abort(long txnID) throws RemoteException {
        this.uncommittedFile.remove(txnID);
        this.txn_filename.remove(txnID);
        return true;
    }

    @Override
    public boolean isAlive() throws RemoteException{
        return true;
    }

    @Override
    public boolean take_charge(String filename, List<ReplicaLoc> replicas) throws RemoteException, NotBoundException {
        List<ReplicaReplicaInterface> stubs = new ArrayList<ReplicaReplicaInterface>();

        for (ReplicaLoc loc : replicas){
            if(loc.getId() == this.id){
                // List has me in it, so we want to skip it
                continue;
            }
            ReplicaReplicaInterface stub;
            if(current_stubs.containsKey(loc.getId())){
                //I got this stub before from another replica
                stub = current_stubs.get(loc.getId());
            }else{
                // I don't have that stub so I need to get it
                stub = (ReplicaReplicaInterface) registry.lookup("Replica"+loc.getId());
                current_stubs.put(loc.getId(), stub);
            }
            // add it to the list
            stubs.add(stub);
        }
        // now each primary know its slaves
        slavesReplicas.put(filename, stubs);
        return true;
    }

    private List<Map.Entry<Long, FileContent>> getSortedActions(Map<Long, FileContent> fileMsgs){
        List<Map.Entry<Long, FileContent>> entries = new ArrayList<>(fileMsgs.entrySet());
        Collections.sort(entries, Comparator.comparingLong(Map.Entry::getKey));
        return entries;
    }


    @Override
    public boolean reflect_data(String filename, byte[] data) throws IOException {
        // tell the slave to write their copy on the hard disk
        lock_manager.putIfAbsent(filename, new ReentrantReadWriteLock());
        ReentrantReadWriteLock lock = lock_manager.get(filename);

        lock.writeLock().lock(); // don't release lock here .. making sure coming reads can't proceed
        FileHandler.write(dir + File.separator + filename, data);
        return true;
    }

    @Override
    public boolean release_file_locks(String filename) {
        // tell the slaves to release the locks of that file
        ReentrantReadWriteLock lock = lock_manager.get(filename);
        lock.writeLock().unlock();
        return false;
    }
}
