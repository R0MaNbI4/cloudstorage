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
        } else if (response.getCommand() == Command.DELETE) {
            delete(ctx, response);
        }
    }

    private void load(ChannelHandlerContext ctx, Response response) throws Exception {
        System.out.println(response.errorInfo().isSuccessful());
        if (response.errorInfo().isSuccessful()) {
            writePartFile(response);
            if (!response.partFileInfo().isLastPart()) {
                Request request = new Request(response);
                request.partFileInfo().addPosition(response, BUFFER_SIZE);
                ctx.writeAndFlush(request);
            }
        } else {
            if (response.errorInfo().isFileNotExists()) {
                System.out.println("File not exists");
            }
        }
    }

    private void save(ChannelHandlerContext ctx, Response response) throws Exception {
        ctx.writeAndFlush(readPartFile(response));
    }

    private void delete(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.errorInfo().isSuccessful()) {
            System.out.println("deleted");
        } else {
            System.out.println("not deleted");
        }
    }

    private void writePartFile(Response response) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFilename(), "rw")) {
            randomAccessFile.seek(response.partFileInfo().getPosition());
            randomAccessFile.write(response.partFileInfo().getFile());
        }
    }

    private Request readPartFile(Response response) throws Exception {
        Request request = new Request(response);
        request.partFileInfo().setPosition(response);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (RandomAccessFile accessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFilename(), "r")) {
            accessFile.seek(response.partFileInfo().getPosition());
            int read = accessFile.read(buffer);
            if (read < buffer.length - 1) {
                byte[] tempBuffer = new byte[read];
                System.arraycopy(buffer, 0, tempBuffer, 0, read);
                request.partFileInfo().setFile(tempBuffer);
                request.partFileInfo().setLastPart(true);
            } else {
                request.partFileInfo().setFile(buffer);
                request.partFileInfo().setLastPart(false);
            }
        }
        return request;
    }
}
