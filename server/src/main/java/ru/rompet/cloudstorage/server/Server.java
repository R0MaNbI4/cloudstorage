package ru.rompet.cloudstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

public class Server {
    public Server(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(10);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new SimpleChannelInboundHandler<Person>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext channelHandlerContext, Person person) throws Exception {
                                            System.out.println(person.getName());
                                        }
                                    }
                            );
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = bootstrap.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
