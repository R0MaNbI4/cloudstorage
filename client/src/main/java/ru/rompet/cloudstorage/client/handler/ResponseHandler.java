package ru.rompet.cloudstorage.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.client.Client;
import ru.rompet.cloudstorage.common.data.DirectoryStructure;
import ru.rompet.cloudstorage.common.data.DirectoryStructureEntry;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.enums.Parameter;

import static ru.rompet.cloudstorage.common.IO.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ResponseHandler extends SimpleChannelInboundHandler<Response> {
    private final String DEFAULT_FILE_LOCATION = "clientFiles\\";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        switch (response.getCommand()) {
            case LOAD -> load(ctx, response);
            case SAVE -> save(ctx, response);
            case DELETE -> delete(ctx, response);
            case CREATE -> create(ctx, response);
            case DIR -> dir(ctx, response);
            case AUTH -> auth(ctx, response);
            case REGISTER -> register(ctx, response);
        }
    }

    private void load(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            if (response.getPartFileInfo().isFirstPart()) {
                createParentDirectories(response, DEFAULT_FILE_LOCATION);
            }
            writePartFile(response, DEFAULT_FILE_LOCATION);
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
        if (response.getErrorInfo().isFileAlreadyExists()) {
            System.out.println("File already exists\nUse parameter -rw to rewrite file or -rn to save file with a different name");
        } else {
            if (Files.exists(path)) {
                if (Files.isRegularFile(path)) {
                    if (response.hasParameter(Parameter.RN) && response.getPartFileInfo().isFirstPart()) {
                        response.setToPath(rename(response.getToPath(), true));
                    }
                    Request request = (Request) readPartFile(response, DEFAULT_FILE_LOCATION);
                    ctx.writeAndFlush(request);
                } else if (Files.isDirectory(path)) {
                    if (response.hasParameter(Parameter.RN)) {
                        response.setToPath(rename(response.getToPath(), false));
                        response.removeParameter(Parameter.RN);
                    }
                    response.addToPaths("\\");
                    List<Path> filePaths = DirectoryStructure.listFiles(response, DEFAULT_FILE_LOCATION, false);
                    for (Path filePath : filePaths) {
                        Request request = new Request(response);
                        request.addToPaths(filePath.toString());
                        ctx.writeAndFlush(request);
                    }
                }
            } else {
                System.out.println("File is not exists");
            }
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

    private void create(ChannelHandlerContext ctx, Response response) throws Exception {
        if (!response.getErrorInfo().isSuccessful()) {
            if (response.getErrorInfo().isFileNotExists()) {
                System.out.println("You can create only one directory");
            }
        } else {
            System.out.println("Successful");
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
}