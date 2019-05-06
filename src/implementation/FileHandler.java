package implementation;

import java.io.*;

public class FileHandler {

    public static byte[] read(String fileName) {
        File file = new File(fileName);

        // Using java.io.FileInputStream
        return readFileToByteArray(file);
    }

    public static void write(String fileName, byte[] data){
        File file = new File(fileName);
        writeFile(file, data);
    }

    private static void writeFile(File file, byte[] data){
        try {
            FileOutputStream fio = new FileOutputStream(file);
            fio.write(data);
            fio.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private static byte[] readFileToByteArray(File file){
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        try{
            fis = new FileInputStream(file);
            fis.read(bArray);
            fis.close();

        }catch(IOException ioExp){
            ioExp.printStackTrace();
        }
        return bArray;
    }
}
