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

import java.util.Scanner;

public class Client{
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
            while (true) {
                Request request = new Request();
                ConsoleInputHandler consoleInputHandler = new ConsoleInputHandler(scanner.nextLine());
                request.setCommand(consoleInputHandler.getCommand());
                request.setFilename(consoleInputHandler.getFilename());
                f.channel().writeAndFlush(request);
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
