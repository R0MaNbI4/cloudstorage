package ru.rompet.cloudstorage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public Client(String host, int port) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap client = new Bootstrap();
        client.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.pipeline().addLast(
                                new ObjectEncoder()
                        );
                    }
                });
        ChannelFuture f = client.connect(host, port).sync();
        System.out.println("Client started");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            f.channel().writeAndFlush(new Person(scanner.next()));
            System.out.println("Object sent");
        }
    }
}