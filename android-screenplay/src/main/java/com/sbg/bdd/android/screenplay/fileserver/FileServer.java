package com.sbg.bdd.android.screenplay.fileserver;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    private final File root;
    private final int port;
    private Thread thread;
    private ServerSocket listener;

    public FileServer(File root, int port) {

        this.root = root;
        this.port = port;
    }

    enum Command {
        LIST, WRITE, READ
    }

    public void stop() {
        try {
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        thread = new Thread() {
            @Override
            public void run() {
                try {
                    startServerSocket();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        thread.start();
    }

    private void startServerSocket() throws IOException {
        listener = new ServerSocket(port, 50, InetAddress.getByName("0.0.0.0"));
        try {
            while (true) {
                final Socket socket = listener.accept();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            serveClient(socket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

    private void serveClient(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            Command command = Command.valueOf(line);
            switch (command) {
                case LIST:
                    processList(reader, writer);
                    break;
                case READ:
                    processRead(reader, writer);
                    break;
                case WRITE:

                    processWrite(reader);
                    break;
            }
        }
    }

    private void processWrite(BufferedReader reader) throws IOException {
        String path = reader.readLine();
        System.out.println("Writing: " + path);
        File file = new File(root, path);
        byte[] bytes = Base64.decodeBase64(reader.readLine().getBytes());
        file.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
            fos.flush();
        }
    }

    private void processRead(BufferedReader reader, BufferedWriter writer) throws IOException {
        String path = reader.readLine();
        System.out.println("Reading: " + path);
        File file = new File(root, path);
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] chunk = new byte[128];
        int chunkLength = -1;
        while ((chunkLength = fis.read(chunk)) > 0) {
            baos.write(chunk, 0, chunkLength);
        }
        writer.write(new String(Base64.encodeBase64(baos.toByteArray())));
        writer.newLine();
        writer.flush();
    }

    private void processList(BufferedReader reader, BufferedWriter writer) throws IOException {
        String path = reader.readLine();
        System.out.println("Listing: " + path);

        File dir = root;
        if (!path.isEmpty()) {
            dir = new File(root, path);
        }
        if (dir.exists() && dir.isDirectory()) {
            File[] list = dir.listFiles();
            for (File file : list) {
                writer.write(file.getName());
                writer.write("|");
                writer.write(file.isDirectory() ? "DIR" : "FRW");
                writer.newLine();
            }
        }
        writer.write("done");
        writer.newLine();
        writer.flush();
    }
}
