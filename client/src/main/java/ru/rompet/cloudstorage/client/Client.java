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
import ru.rompet.cloudstorage.client.handler.ResponseHandler;
import ru.rompet.cloudstorage.client.handler.JsonDecoder;
import ru.rompet.cloudstorage.client.handler.JsonEncoder;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.Settings;
import ru.rompet.cloudstorage.common.enums.Command;
import ru.rompet.cloudstorage.common.enums.Parameter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Scanner;

import static ru.rompet.cloudstorage.common.IO.*;

public class Client{
    private static boolean authenticated = false;
    private static String login = "";
    private final int MAX_FRAME_LENGTH = 1024 * 1024;
    private final int LENGTH_FIELD_LENGTH = 8;

    public Client(String host, int port) throws Exception {
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
                                    new ResponseHandler()
                            );
                        }
                    });

            ChannelFuture f = client.connect(host, port).sync();

            System.out.println("Client started");

            Scanner scanner = new Scanner(System.in);
            ConsoleInputHandler input = new ConsoleInputHandler();
            while (true) {
                if (input.validate(scanner.nextLine())) {
                    if (input.getCommand() == Command.HELP) {
                        System.out.println(new HelpMessage());
                    } else if (input.getCommand() == Command.CHROOT) {
                        if (Settings.setRoot(input.getFromPath())) {
                            System.out.println("Successful");
                        }
                    } else if (input.getCommand() == Command.SHROOT) {
                        System.out.println(Settings.getRoot());
                    } else {
                        Request request = new Request(input.getCommand());
                        request.setAuthenticated(authenticated);
                        if (input.getParameters().size() > 0) {
                            request.setParameters(input.getParameters());
                        }
                        if (input.getCommand() == Command.AUTH ||
                                input.getCommand() == Command.REGISTER) {
                            request.getCredentials().setLogin(input.getLogin());
                            request.getCredentials().setPassword(input.getPassword());
                            if (request.isAuthenticated()) {
                                System.out.println("You are already authenticated");
                                continue;
                            }
                        } else {
                            request.setFromPath(input.getFromPath());
                            request.setToPath(input.getToPath());
                            if (request.isAuthenticated()) {
                                request.getCredentials().setLogin(login);
                            } else {
                                System.out.println("You are not authenticated\nUse command 'auth login pass'");
                                continue;
                            }
                            if (request.getCommand() == Command.LOAD && !request.hasParameter(Parameter.CD)
                                    && !isPathExists(request, "clientFiles\\")) {
                                System.out.println(("Path is not exists. Use parameter -cd to create all necessary directories"));
                                continue;
                            }
                        }
                        f.channel().writeAndFlush(request);
                    }
                } else {
                    if (!input.isValidCommand()) {
                        System.out.println("Invalid command");
                    } else if (!input.isValidPath()) {
                        System.out.println("Invalid path");
                    } else if (!input.isValidCredentials()) {
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
