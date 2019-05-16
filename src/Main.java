import concurrency.TestMultipleClients;
import implementation.Client;
import implementation.Master;
import implementation.Replica;
import implementation.ReplicaLoc;
import interfaces.MasterClientInterface;
import interfaces.ReplicaClientInterface;
import utils.Configuration;
import utils.FileContent;
import utils.MessageNotFoundException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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
    private static List<Replica> replicaServers;
    private static List<ReplicaLoc> replicaLocs;
    private static Registry registry;


    public static void main(String[] args) throws IOException, AlreadyBoundException {

        if(args.length  == 2){
            Configuration.REG_ADDR = args[0];
            Configuration.REG_PORT = Integer.parseInt(args[1]);
        }

        registry = LocateRegistry.createRegistry(Configuration.REG_PORT);
        getReplica();
        startMaster();

        /*
        testOneFile();
        testThreeFiles();
        partialUpdateOrUncommitted();
        Thread t0 = new Thread(new TestMultipleClients());
        Thread t1 = new Thread(new TestMultipleClients());
        Thread t2 = new Thread(new TestMultipleClients());
        Thread t3 = new Thread(new TestMultipleClients());
        t0.start();
        t1.start();
        t2.start();
        t3.start();
         */
    }

    private static void startMaster() throws RemoteException, AlreadyBoundException {

        master = new Master(replicaServers, replicaLocs);
        MasterClientInterface stub = (MasterClientInterface) UnicastRemoteObject.exportObject(master, Configuration.REG_PORT);
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
            Replica rs = new Replica(i, s);

            ReplicaClientInterface stub = (ReplicaClientInterface) UnicastRemoteObject.exportObject(rs, Configuration.REG_PORT);

            registry.rebind("Replica" + i, rs);

            replicaServers.add(rs);
            replicaLocs.add(replicaLoc);
        }
        br.close();
    }

    private static void testOneFile(){
        writeFile("File1.txt", "Test aloo aloo aloo.");
    }

    private static void testThreeFiles(){
        /******************Test on file 1********************/
        writeFile("File2.txt", "test2 test test test");

        /******************Test on file 1********************/
        writeFile("File3.txt", "test3 test test test aloooooooo");

        /*******************Test on file 2****************/
        writeFile("File4.txt", "test2 test test test");
    }

    private static void partialUpdateOrUncommitted(){
        try {
            Client c = new Client();
            char[] ss = "test uncommitted.".toCharArray();
            byte[] data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write(new FileContent("test.txt", data));
            handle_read("test.txt", c.read("test.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void handle_read(String filename, byte[] ret){
        if(ret == null){
            // file not found
            System.out.println("File not found!");
        }else {
            System.out.println(filename + ": " + new String(ret));
        }
    }

    private static void writeFile(String fileName, String str){
        try {
            Client c = new Client();
            char[] ss = str.toCharArray();
            byte[] data = new byte[ss.length];
            for (int i = 0; i < ss.length; i++)
                data[i] = (byte) ss[i];

            c.write(new FileContent(fileName, data));
            handle_read(fileName, c.read(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (MessageNotFoundException e) {
            e.printStackTrace();
        }
    }

}
