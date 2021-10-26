package ru.rompet.cloudstorage.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.client.Client;
import ru.rompet.cloudstorage.common.data.DirectoryStructure;
import ru.rompet.cloudstorage.common.data.DirectoryStructureEntry;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.enums.Parameter;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ResponseHandler extends SimpleChannelInboundHandler<Response> {
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
            case REGISTER -> register(ctx, response);
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
        Path path = Path.of(DEFAULT_FILE_LOCATION + response.getFromPath());
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                ctx.writeAndFlush(readPartFile(response));
            } else if (Files.isDirectory(path)) {
                response.setFromPath(response.getFromPath() + "\\");
                response.setToPath(response.getToPath() + "\\");
                List<Path> filePaths = DirectoryStructure.listFiles(
                        Path.of(response.getFromPath()),
                        Path.of(DEFAULT_FILE_LOCATION),
                        response.hasParameter(Parameter.R),
                        false);
                for (Path filePath : filePaths) {
                    Request request = new Request(response);
                    request.setFromPath(response.getFromPath() + filePath.toString());
                    request.setToPath(response.getToPath() + filePath.toString());
                    ctx.writeAndFlush(request);
                }
            }
        } else {
            System.out.println("File is not exists");
        }
    }

    private void delete(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            System.out.println("deleted");
        } else {
            if (response.getErrorInfo().isFileNotExists()) {
                System.out.println("File not exists");
            } else if (response.getErrorInfo().isFileUnableToDelete()) {
                System.out.println("File " + response.getErrorInfo().getErrorDetails() + " unable to delete");
            } else {
                System.out.println("Not deleted");
            }
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

    private void register(ChannelHandlerContext ctx, Response response) {
        if (response.getErrorInfo().isSuccessful()) {
            System.out.println("Successful");
        } else {
            System.out.println("Registration failed");
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
            if (read == -1) {
                request.getPartFileInfo().setFile(new byte[0]);
                request.getPartFileInfo().setLastPart(true);
            } else if (read < buffer.length - 1) {
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
