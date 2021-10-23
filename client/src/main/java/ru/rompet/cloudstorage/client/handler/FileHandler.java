package ru.rompet.cloudstorage.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.client.Client;
import ru.rompet.cloudstorage.common.data.DirectoryStructureEntry;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler extends SimpleChannelInboundHandler<Response> {
    private final int BUFFER_SIZE = 1024 * 512;
    private final String DEFAULT_FILE_LOCATION = "clientFiles\\";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        switch (response.getCommand()) {
            case LOAD -> load(ctx, response);
            case SAVE -> save(ctx, response);
            case DELETE -> delete(ctx, response);
            case DIR -> dir(ctx, response);
            case AUTH -> auth(ctx, response);
        }
    }

    private void load(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            createDirectoryIfNotExists(response);
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

    private void dir(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            for (DirectoryStructureEntry entry : response.getDirectoryStructure()) {
                System.out.println(entry.getName() + "\t" + entry.getSizeInBytes() + "\t" + entry.isDirectory());
            }
        } else if (response.getErrorInfo().isFileNotExists()) {
            System.out.println("Path is not exists");
        }
    }

    private void auth(ChannelHandlerContext ctx, Response response) {
        if (response.isAuthenticated()) {
            Client.setLogin(response.getCredentials().getLogin());
            Client.setAuthenticated(response.isAuthenticated());
            System.out.println("Successful");
        } else {
            System.out.println("Authentication failed");
        }
    }

    private void writePartFile(Response response) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getToPath(), "rw")) {
            randomAccessFile.seek(response.getPartFileInfo().getPosition());
            randomAccessFile.write(response.getPartFileInfo().getFile());
        }
    }

    private Request readPartFile(Response response) throws Exception {
        Request request = new Request(response);
        request.getPartFileInfo().setPosition(response);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (RandomAccessFile accessFile = new RandomAccessFile(
                DEFAULT_FILE_LOCATION + response.getFromPath(), "r")) {
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

    private void createDirectoryIfNotExists(Response response) throws Exception {
        if (response.getPartFileInfo().isFirstPart()) {
            Path path = Path.of(DEFAULT_FILE_LOCATION + response.getToPath()).getParent();
            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }
        }
    }
}
