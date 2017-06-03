package com.sbg.bdd.android.screenplay.fileserver;

import java.io.File;
import java.io.IOException;

public class FileServerRunner {
    public static void main(String[] args) throws IOException {
        File root = new File(".").getAbsoluteFile();
        int port = args.length == 0 ? 9999 : Integer.parseInt(args[0]);
        new FileServer(root, port).start();
    }
}
