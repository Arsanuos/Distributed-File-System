package implementation;

import interfaces.ReplicaServerClientInterface;
import interfaces.ReplicaServerMasterInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

public class ReplicaServer implements ReplicaServerClientInterface, ReplicaServerMasterInterface {

    private int id;
    private String dir;
    private String addr;
    private Map<Long, Map<Long, FileContent>> uncommittedFile;

    public ReplicaServer(int id, String addr){
        this.id = id;
        this.dir = "./replica-" + id;
        this.addr = addr;
        this.uncommittedFile = new HashMap<>();
        File file = new File(this.dir);
        if(!file.exists()){
            file.mkdir();
        }

    }
    @Override
    public WriteMsg write(long txnID, long msgSeqNum, FileContent data) throws RemoteException, IOException {
        //Don't know where to throw IOException.
        //Replica must know the other replicas.

        if(uncommittedFile.containsKey(txnID)){
            uncommittedFile.get(txnID).put(msgSeqNum, data);
        }else{
            Map<Long, FileContent> tmp = new HashMap<>();
            tmp.put(msgSeqNum, data);
            uncommittedFile.put(txnID, tmp);
        }
        return new WriteMsg(txnID, msgSeqNum, new ReplicaLoc(this.id, this.addr, true));
    }

    @Override
    public FileContent read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        return null;
    }

    @Override
    public boolean commit(long txnID, long numOfMsgs) throws MessageNotFoundException, RemoteException {
        if(!this.uncommittedFile.containsKey(txnID)){
            throw new MessageNotFoundException();
        }
        Map<Long, FileContent> tmp = this.uncommittedFile.get(txnID);
        List<Map.Entry<Long, FileContent>> sortedList = getSortedActions(tmp);
        for (Map.Entry<Long, FileContent> e : sortedList) {
            FileContent content = e.getValue();
            try {
                FileHandler.write(content.getFileName(), content.getData());
            } catch (IOException e1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean abort(long txnID) throws RemoteException {
        this.uncommittedFile.remove(txnID);
        return true;
    }

    @Override
    public boolean isAlive() throws RemoteException{
        return true;
    }

    private List<Map.Entry<Long, FileContent>> getSortedActions(Map<Long, FileContent> fileMsgs){
        List<Map.Entry<Long, FileContent>> entries = new ArrayList<>(fileMsgs.entrySet());
        Collections.sort(entries, Comparator.comparingLong(Map.Entry::getKey));
        return entries;
    }
}
