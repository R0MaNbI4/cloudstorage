package ru.rompet.cloudstorage.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;

import java.io.RandomAccessFile;

public class FileHandler extends SimpleChannelInboundHandler<Response> {
    private final int BUFFER_SIZE = 1024 * 512;
    private final String DEFAULT_FILE_LOCATION = "clientFiles\\";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        switch (response.getCommand()) {
            case LOAD -> load(ctx, response);
            case SAVE -> save(ctx, response);
            case DELETE -> delete(ctx, response);
        }
    }

    private void load(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            writePartFile(response);
            if (!response.getPartFileInfo().isLastPart()) {
                Request request = new Request(response);
                request.getPartFileInfo().addPosition(response, BUFFER_SIZE);
                ctx.writeAndFlush(request);
            }
        } else {
            if (response.getErrorInfo().isFileNotExists()) {
                System.out.println("File not exists");
            }
        }
    }

    private void save(ChannelHandlerContext ctx, Response response) throws Exception {
        ctx.writeAndFlush(readPartFile(response));
    }

    private void delete(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            System.out.println("deleted");
        } else {
            System.out.println("not deleted");
        }
    }

    private void writePartFile(Response response) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFilename(), "rw")) {
            randomAccessFile.seek(response.getPartFileInfo().getPosition());
            randomAccessFile.write(response.getPartFileInfo().getFile());
        }
    }

    private Request readPartFile(Response response) throws Exception {
        Request request = new Request(response);
        request.getPartFileInfo().setPosition(response);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (RandomAccessFile accessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFilename(), "r")) {
            accessFile.seek(response.getPartFileInfo().getPosition());
            int read = accessFile.read(buffer);
            if (read < buffer.length - 1) {
                byte[] tempBuffer = new byte[read];
                System.arraycopy(buffer, 0, tempBuffer, 0, read);
                request.getPartFileInfo().setFile(tempBuffer);
                request.getPartFileInfo().setLastPart(true);
            } else {
                request.getPartFileInfo().setFile(buffer);
                request.getPartFileInfo().setLastPart(false);
            }
        }
        return request;
    }
}
