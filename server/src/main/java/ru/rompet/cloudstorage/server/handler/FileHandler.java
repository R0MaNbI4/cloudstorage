package ru.rompet.cloudstorage.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.common.Command;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;

import java.io.File;
import java.io.RandomAccessFile;

public class FileHandler extends SimpleChannelInboundHandler<Request> {
    private final int BUFFER_SIZE = 1024 * 512;
    private final String DEFAULT_FILE_LOCATION = "serverFiles\\";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        if (request.getCommand() == Command.LOAD) {
            load(ctx, request);
        } else if (request.getCommand() == Command.SAVE) {
            save(ctx, request);
        } else if (request.getCommand() == Command.DELETE) {
            delete(ctx, request);
        } else if (request.getCommand() == Command.UPDATE) {
            update(ctx, request);
        }
    }

    private void load(ChannelHandlerContext ctx, Request request) throws Exception {
        byte[] buffer = new byte[BUFFER_SIZE];
        Response response = new Response();
        response.setCommand(request.getCommand());
        response.setFilename(request.getFilename());
        response.setPosition(request.getPosition());
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + request.getFilename(), "r")) {
            randomAccessFile.seek(request.getPosition());
            int read = randomAccessFile.read(buffer);
            if (read < buffer.length - 1) {
                byte[] tempBuffer = new byte[read];
                System.arraycopy(buffer, 0, tempBuffer, 0, read);
                response.setFile(tempBuffer);
                response.setLastPart(true);
            } else {
                response.setFile(buffer);
                response.setLastPart(false);
            }
            ctx.writeAndFlush(response);
        }
    }

    private void save(ChannelHandlerContext ctx, Request request) throws Exception {
        if (request.getCommand() == Command.UPDATE) {
            request.setCommand(Command.SAVE);
        }
        if (!request.hasData()) {
            Response response = new Response();
            response.setCommand(request.getCommand());
            response.setFilename(request.getFilename());
            ctx.writeAndFlush(response);
            return;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + request.getFilename(), "rw")) {
            randomAccessFile.seek(request.getPosition());
            randomAccessFile.write(request.getFile());
        }
        if (!request.isLastPart()) {
            Response response = new Response();
            response.setCommand(request.getCommand());
            response.setFilename(request.getFilename());
            response.setPosition(request.getPosition() + BUFFER_SIZE);
            ctx.writeAndFlush(response);
        }
    }

    private boolean delete(ChannelHandlerContext ctx, Request request) throws Exception {
        File file = new File(DEFAULT_FILE_LOCATION + request.getFilename());
        Response response = new Response();
        response.setCommand(request.getCommand());
        if (file.delete()) {
            response.setSuccessful(true);
            ctx.writeAndFlush(response);
            return true;
        } else {
            response.setSuccessful(false);
            ctx.writeAndFlush(response);
            return false;
        }
    }

    private void update(ChannelHandlerContext ctx, Request request) throws Exception {
        if (delete(ctx, request)) {
            save(ctx, request);
        }
    }
}
