import implementation.*;
import interfaces.MasterServerClientInterface;
import interfaces.ReplicaServerMasterInterface;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static Registry registry;
    private static final String replica_filename = "repServers.txt";
    public static void main(String[] args) throws IOException, MessageNotFoundException, NotBoundException {
        System.out.println("Hello World!");

        String addr = args[0];
        int port = Integer.parseInt(args[1]);
        String dir_path = args[2];

        System.setProperty("java.rmi.server.hostname", addr);

        // create registry for remote invoking
        LocateRegistry.createRegistry(port);
        registry = LocateRegistry.getRegistry(port);

        // create master, and slaves
        server_creation(port);

        // create clients (Test server)
        client_creation(addr, port);
    }

    private static void server_creation(int port) throws IOException {
        Master m = start_master(port);
        replica_creation(m);
        System.out.println("Finished Initialization of Server side");
    }

    private static void replica_creation(Master m) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(replica_filename));
        int n = Integer.parseInt(br.readLine().trim());
        String s;
        List<ReplicaLoc> replicaLocs = new ArrayList<>();
        List<ReplicaServerMasterInterface> replicaStubs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            s = br.readLine().trim();
            String addr = s.substring(0, s.indexOf(':'));
            int port = Integer.parseInt(s.substring(s.indexOf(':')));
            ReplicaLoc rl = new ReplicaLoc(i, addr , true);
            ReplicaServer rs = new ReplicaServer(i, addr);

             ReplicaServerMasterInterface stub = (ReplicaServerMasterInterface) UnicastRemoteObject.exportObject(rs, port);
            registry.rebind("Replica"+i, stub);
            System.out.println("replica server " + i + " created and alive =  " + rs.isAlive());

            replicaLocs.add(rl);
            replicaStubs.add(stub);
        }

        br.close();
        m.add_replicas(replicaStubs, replicaLocs);
        System.out.println("Replicas has been created");
    }

    private static Master start_master(int port) throws RemoteException {
        Master m = new Master();
        MasterServerClientInterface stub = (MasterServerClientInterface) UnicastRemoteObject.exportObject(m, port);
        registry.rebind("Master", stub);
        System.out.println("Master server Started");
        return m;
    }

    private static void client_creation(String addr, int port) throws NotBoundException, MessageNotFoundException, IOException {
        // simple test one client want to write and read

        Client c = new Client(addr, port);
        char[] ss = "File 1 test test END ".toCharArray();
        byte[] data = new byte[ss.length];
        for (int i = 0; i < ss.length; i++) {
            data[i] = (byte) ss[i];
        }
        String filename = "file1.txt";
        FileContent fc = new FileContent(filename, data);
        c.write(fc);
        byte[] ret = c.read(filename);
        System.out.println(filename + ": " + ret);
    }
}
