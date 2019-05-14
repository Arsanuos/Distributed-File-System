package implementation;

import java.io.*;

public class FileHandler {

    public static byte[] read(String fileName) throws IOException {
        File file = new File(fileName);

        // Using java.io.FileInputStream
        return readFileToByteArray(file);
    }

    public static void write(String fileName, byte[] data) throws IOException {
        File file = new File(fileName);
        writeFile(file, data);
    }

    private static void writeFile(File file, byte[] data) throws IOException {
        FileOutputStream fio = new FileOutputStream(file);
        fio.write(data);
        fio.flush();
        fio.close();

    }


    private static byte[] readFileToByteArray(File file) throws IOException {
        FileInputStream fis = null;
        // Creating a byte array using the length of the file
        // file.length returns long which is cast to int
        byte[] bArray = new byte[(int) file.length()];
        fis = new FileInputStream(file);
        fis.read(bArray);
        fis.close();

        return bArray;
    }
}
