package com.sbg.bdd.android.screenplay.fileserver;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FileClient {
    private final Socket socket;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    int port;
    String host;

    public FileClient(String host,int port) {
        try {
            this.port = port;
            this.host = host;
            socket = new Socket();
            socket.connect(new InetSocketAddress(host,port));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
    public List<String> list(String path){
        try {
            List<String> result = new ArrayList<>();
            writer.write(FileServer.Command.LIST.name());
            writer.newLine();
            writer.write(path);
            writer.newLine();
            writer.flush();
            String line = null;
            while(!(line=reader.readLine()).equals("done")){
                result.add(line);
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public byte[] read(String path) {
        try {
            writer.write(FileServer.Command.READ.name());
            writer.newLine();
            writer.write(path);
            writer.newLine();
            writer.flush();
            String dataBase64= reader.readLine();
            return Base64.decodeBase64(dataBase64.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void write(String path, byte[] data) {
        try {
            writer.write(FileServer.Command.WRITE.name());
            writer.newLine();
            writer.write(path);
            writer.newLine();
            writer.write(new String(Base64.encodeBase64(data)));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
