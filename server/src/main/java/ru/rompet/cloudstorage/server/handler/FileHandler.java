package ru.rompet.cloudstorage.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.common.Command;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler extends SimpleChannelInboundHandler<Request> {
    private final int BUFFER_SIZE = 1024 * 512;
    private final String DEFAULT_FILE_LOCATION = "serverFiles\\";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        switch (request.getCommand()) {
            case LOAD -> load(ctx, request);
            case SAVE -> save(ctx, request);
            case DELETE -> delete(ctx, request);
            case UPDATE -> update(ctx, request);
            case DIR -> dir(ctx, request);
        }
    }

    private void load(ChannelHandlerContext ctx, Request request) throws Exception {
        if (Files.exists(Path.of(DEFAULT_FILE_LOCATION + request.getFilename()))) {
            ctx.writeAndFlush(readPartFile(request));
        } else {
            Response response = new Response(request);
            response.getErrorInfo().setSuccessful(false);
            response.getErrorInfo().setFileNotExists(true);
            ctx.writeAndFlush(response);
        }
    }

    private void save(ChannelHandlerContext ctx, Request request) throws Exception {
        if (!request.hasData()) {
            ctx.writeAndFlush(new Response(request));
        } else {
            writePartFile(request);
            if (!request.getPartFileInfo().isLastPart()) {
                Response response = new Response(request);
                response.getPartFileInfo().addPosition(request, BUFFER_SIZE);
                ctx.writeAndFlush(response);
            }
        }
    }

    private boolean delete(ChannelHandlerContext ctx, Request request) throws Exception {
        File file = new File(DEFAULT_FILE_LOCATION + request.getFilename());
        Response response = new Response(request);
        if (file.delete()) {
            ctx.writeAndFlush(response);
            return true;
        } else {
            response.getErrorInfo().setSuccessful(false);
            ctx.writeAndFlush(response);
            return false;
        }
    }

    private void update(ChannelHandlerContext ctx, Request request) throws Exception {
        if (delete(ctx, request)) {
            save(ctx, new Request(Command.SAVE, request.getFilename()));
        }
    }

    private void dir(ChannelHandlerContext ctx, Request request) throws Exception {
        Response response = new Response(request);
        response.getDirectoryStructure().scan(DEFAULT_FILE_LOCATION);
        ctx.writeAndFlush(response);
    }

    private Response readPartFile(Request request) throws Exception {
        Response response = new Response(request);
        response.getPartFileInfo().setPosition(request);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (RandomAccessFile accessFile =
                     new RandomAccessFile(DEFAULT_FILE_LOCATION + request.getFilename(), "r")) {
            accessFile.seek(request.getPartFileInfo().getPosition());
            int read = accessFile.read(buffer);
            if (read < buffer.length - 1) {
                byte[] tempBuffer = new byte[read];
                System.arraycopy(buffer, 0, tempBuffer, 0, read);
                response.getPartFileInfo().setFile(tempBuffer);
                response.getPartFileInfo().setLastPart(true);
            } else {
                response.getPartFileInfo().setFile(buffer);
                response.getPartFileInfo().setLastPart(false);
            }
            return response;
        }
    }

    private void writePartFile(Request request) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + request.getFilename(), "rw")) {
            randomAccessFile.seek(request.getPartFileInfo().getPosition());
            randomAccessFile.write(request.getPartFileInfo().getFile());
        }
    }
}
