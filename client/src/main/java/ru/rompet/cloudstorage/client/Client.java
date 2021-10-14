package ru.rompet.cloudstorage.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import ru.rompet.cloudstorage.client.handler.ConsoleInputHandler;
import ru.rompet.cloudstorage.client.handler.FileHandler;
import ru.rompet.cloudstorage.client.handler.JsonDecoder;
import ru.rompet.cloudstorage.client.handler.JsonEncoder;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.enums.Command;

import java.util.Scanner;

public class Client{
    private static boolean authenticated = false;
    private static String login = "";
    private final int MAX_FRAME_LENGTH = 1024 * 1024;
    private final int LENGTH_FIELD_LENGTH = 8;

    public Client(String host, int port) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap client = new Bootstrap();
            client.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new LengthFieldBasedFrameDecoder(
                                            MAX_FRAME_LENGTH,
                                            0,
                                            LENGTH_FIELD_LENGTH,
                                            0,
                                            LENGTH_FIELD_LENGTH
                                    ),
                                    new LengthFieldPrepender(LENGTH_FIELD_LENGTH),
                                    new ByteArrayDecoder(),
                                    new ByteArrayEncoder(),
                                    new JsonDecoder(),
                                    new JsonEncoder(),
                                    new FileHandler()
                            );
                        }
                    });

            ChannelFuture f = client.connect(host, port).sync();

            System.out.println("Client started");

            Scanner scanner = new Scanner(System.in);
            ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler();
            while (true) {
                if (consoleInputHandler.validate(scanner.nextLine())) {
                    Request request = new Request(consoleInputHandler.getCommand());
                    request.setAuthenticated(authenticated);
                    if (consoleInputHandler.getCommand() == Command.AUTH) {
                        request.getCredentials().setLogin(consoleInputHandler.getLogin());
                        request.getCredentials().setPassword(consoleInputHandler.getPassword());
                        if (request.isAuthenticated()) {
                            System.out.println("You are already authenticated");
                            continue;
                        }
                    } else {
                        request.setFromPath(consoleInputHandler.getFromPath());
                        request.setToPath(consoleInputHandler.getToPath());
                        if (request.isAuthenticated()) {
                            request.getCredentials().setLogin(login);
                        } else {
                            System.out.println("You are not authenticated\nUse command 'auth login pass'");
                            continue;
                        }
                    }
                    f.channel().writeAndFlush(request);
                } else {
                    if (!consoleInputHandler.isValidCommand()) {
                        System.out.println("Invalid command");
                    } else if (!consoleInputHandler.isValidPath()) {
                        System.out.println("Invalid path");
                    } else if (!consoleInputHandler.isValidCredentials()) {
                        System.out.println("Invalid credentials");
                    }
                }
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void setAuthenticated(boolean authenticated) {
        Client.authenticated = authenticated;
    }

    public static void setLogin(String login) {
        Client.login = login;
    }
}
