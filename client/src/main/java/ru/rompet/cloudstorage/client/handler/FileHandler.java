package ru.rompet.cloudstorage.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.common.Command;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;

import java.io.RandomAccessFile;

public class FileHandler extends SimpleChannelInboundHandler<Response> {
    private final int BUFFER_SIZE = 1024 * 512;
    private final String DEFAULT_FILE_LOCATION = "clientFiles\\";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getCommand() == Command.LOAD) {
            load(ctx, response);
        } else if (response.getCommand() == Command.SAVE) {
            save(ctx, response);
        }
    }

    private void load(ChannelHandlerContext ctx, Response response) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFilename(), "rw")) {
            randomAccessFile.seek(response.getPosition());
            randomAccessFile.write(response.getFile());
        }
        if (!response.isLastPart()) {
            Request request = new Request();
            request.setCommand(Command.LOAD);
            request.setFilename(response.getFilename());
            request.setPosition(response.getPosition() + BUFFER_SIZE);
            ctx.writeAndFlush(request);
        }
    }

    private void save(ChannelHandlerContext ctx, Response response) throws Exception {
        Request request = new Request();
        request.setPosition(response.getPosition());
        request.setFilename(response.getFilename());
        request.setCommand(response.getCommand());
        byte[] buffer = new byte[BUFFER_SIZE];
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFilename(), "r")) {
            randomAccessFile.seek(response.getPosition());
            int read = randomAccessFile.read(buffer);
            if (read < buffer.length - 1) {
                byte[] tempBuffer = new byte[read];
                System.arraycopy(buffer, 0, tempBuffer, 0, read);
                request.setFile(tempBuffer);
                request.setLastPart(true);
            } else {
                request.setFile(buffer);
                request.setLastPart(false);
            }
            ctx.writeAndFlush(request);
        }
    }
}
