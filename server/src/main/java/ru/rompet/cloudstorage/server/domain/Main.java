package ru.rompet.cloudstorage.server.domain;

import java.net.SocketException;

public class Main {
    public static void main(String[] args) {
        int port = 9000;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        try {
            new Server(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("SWW");
        }
    }
}
