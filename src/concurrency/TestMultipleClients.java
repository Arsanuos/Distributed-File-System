package concurrency;

import implementation.Client;
import utils.FileContent;
import utils.MessageNotFoundException;

import java.io.IOException;
import java.rmi.NotBoundException;

public class TestMultipleClients implements Runnable {

    @Override
    public void run() {
        try {
            String fileName = "fileCon.txt";
            Client c = new Client();
            char[] ss = "Test concurrency multiple clients".toCharArray();
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

    private void handle_read(String filename, byte[] ret){
        if(ret == null){
            // file not found
            System.out.println("File not found!");
        }else {
            System.out.println(filename + ": " + new String(ret));
        }
    }
}
