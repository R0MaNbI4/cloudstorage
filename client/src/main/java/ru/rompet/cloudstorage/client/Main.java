package ru.rompet.cloudstorage.client;

import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {
        try {
            new Client(InetAddress.getLoopbackAddress().getHostName(), 9000);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("SWW");
        }
    }
}