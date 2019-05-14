import implementation.*;
import interfaces.MasterServerClientInterface;
import interfaces.ReplicaServerClientInterface;

import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static Master master;
    private static List<ReplicaServer> replicaServers;
    private static List<ReplicaLoc> replicaLocs;
    private static Registry registry;


    public static void main(String[] args) throws IOException, AlreadyBoundException {

        registry = LocateRegistry.createRegistry(Configuration.REG_PORT);

        getReplica();
        startMaster();
        startClient();
    }

    private static void startMaster() throws RemoteException, AlreadyBoundException {

        master = new Master(replicaServers, replicaLocs);
        MasterServerClientInterface stub = (MasterServerClientInterface) UnicastRemoteObject.exportObject(master, Configuration.REG_PORT);
        registry.bind("Master", master);
    }

    private static void getReplica() throws IOException {
        replicaServers = new ArrayList<>(Configuration.NUM_REPLICA);
        replicaLocs = new ArrayList<>(Configuration.NUM_REPLICA);
        BufferedReader br = new BufferedReader(new FileReader("repServers.txt"));
        int n = Integer.parseInt(br.readLine().trim());
        ReplicaLoc replicaLoc;
        String s;

        for (int i = 0; i < n; i++) {
            s = br.readLine().trim();
            replicaLoc = new ReplicaLoc(i,  s, true);
            ReplicaServer rs = new ReplicaServer(i, s);

            ReplicaServerClientInterface stub = (ReplicaServerClientInterface) UnicastRemoteObject.exportObject(rs, Configuration.REG_PORT);

            registry.rebind("Replica" + i, rs);

            replicaServers.add(rs);
            replicaLocs.add(replicaLoc);
        }
        br.close();
    }

    private static void startClient(){
        try {
            Client c = new Client();
            char[] ss = "File 1 test test END ".toCharArray();
            byte[] data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write(new FileContent("file1.txt", data));
            byte[] ret = c.read("file1.txt");
            System.out.println("file1.txt: " + new String(ret));
        /*
            c = new Client();
            ss = "File 1 Again Again END ".toCharArray();
            data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write(new FileContent("file1", data));
            ret = c.read("file1");
            System.out.println("file1: " + ret);

            c = new Client();
            ss = "File 2 test test END ".toCharArray();
            data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write(new FileContent("file1", data));
            ret = c.read("file2");
            System.out.println("file2: " + ret);
        */
        } catch (NotBoundException | IOException | MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

}
