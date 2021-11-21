package ru.rompet.cloudstorage.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.rompet.cloudstorage.client.Client;
import ru.rompet.cloudstorage.common.FileStateTracker;
import ru.rompet.cloudstorage.common.Settings;
import ru.rompet.cloudstorage.common.transfer.data.DirectoryStructure;
import ru.rompet.cloudstorage.common.transfer.data.DirectoryStructureEntry;
import ru.rompet.cloudstorage.common.transfer.Response;
import ru.rompet.cloudstorage.common.transfer.Request;
import ru.rompet.cloudstorage.common.enums.Parameter;

import static ru.rompet.cloudstorage.common.Utils.*;
import static ru.rompet.cloudstorage.common.transfer.IO.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ResponseHandler extends SimpleChannelInboundHandler<Response> {
    private String rootDirectory = Settings.getRoot();
    private final FileStateTracker fileStateTracker = new FileStateTracker();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        switch (response.getCommand()) {
            case LOAD -> load(ctx, response);
            case SAVE -> save(ctx, response);
            case DELETE -> delete(ctx, response);
            case CREATE -> create(ctx, response);
            case MOVE -> move(ctx, response);
            case DIR -> dir(ctx, response);
            case AUTH -> auth(ctx, response);
            case REGISTER -> register(ctx, response);
        }
    }

    private void load(ChannelHandlerContext ctx, Response response) throws Exception {
        if (response.getErrorInfo().isSuccessful()) {
            if (response.getPartFileInfo().isFirstPart()) {
                createParentDirectories(response, rootDirectory);
            }
            fileStateTracker.sync(response, rootDirectory);
            writePartFile(response, rootDirectory);
            if (!response.getPartFileInfo().isLastPart()) {
                Request request = new Request(response);
                request.getPartFileInfo().addPosition(response, BUFFER_SIZE);
                ctx.writeAndFlush(request);
            }
        } else {
            if (response.getErrorInfo().isFileNotExists()) {
                System.out.println("File not exists");
            } else if (response.getErrorInfo().isFileLock()) {
                System.out.println("File has not been uploaded yet");
            } else if (response.getErrorInfo().isImpossibleUniquelyIdentifyFileException()) {
                System.out.println(response.getErrorInfo().getErrorDetails());
            }
        }
    }

    private void save(ChannelHandlerContext ctx, Response response) throws Exception {
        Path path = Path.of(rootDirectory + response.getFromPath());
        if (response.getErrorInfo().isSuccessful()) {
            if (Files.exists(path)) {
                boolean isFile = Files.isRegularFile(path);
                if (!fileStateTracker.isLock(response, rootDirectory, isFile)) {
                    if (isFile) {
                        if (response.hasParameter(Parameter.RN) && response.getPartFileInfo().isFirstPart()) {
                            response.setToPath(rename(response.getToPath(), true));
                        }
                        Request request = (Request) readPartFile(response, rootDirectory);
                        ctx.writeAndFlush(request);
                    } else {
                        if (!response.hasParameter(Parameter.CD)) {
                            response.addParameter(Parameter.CD);
                        }
                        if (response.hasParameter(Parameter.RN)) {
                            response.setToPath(rename(response.getToPath(), false));
                            response.removeParameters(Parameter.RN);
                        }
                        response.addToPaths("\\");
                        List<Path> filePaths = DirectoryStructure.listFiles(response, rootDirectory, false);
                        for (Path filePath : filePaths) {
                            Request request = new Request(response);
                            request.addToPaths(filePath.toString());
                            ctx.writeAndFlush(request);
                        }
                    }
                } else {
                    System.out.println("File has not been downloaded yet");
                }
            } else {
                System.out.println("File is not exists");
            }
        } else {
            if (response.getErrorInfo().isFileAlreadyExists()) {
                System.out.println("File already exists\nUse parameter -rw to rewrite file or -rn to save file with a different name");
            } else if (response.getErrorInfo().isPathNotExists()) {
                System.out.println("Path is not exists. Use parameter -cd to create all necessary directories");
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
            } else if (response.getErrorInfo().isFileLock()){
                System.out.println("File has not been uploaded yet");
            } else {
                System.out.println("Not deleted");
            }
        }
    }

    private void create(ChannelHandlerContext ctx, Response response) throws Exception {
        if (!response.getErrorInfo().isSuccessful()) {
            if (response.getErrorInfo().isPathNotExists()) {
                System.out.println("You can create only one directory. Use parameter -r to create multiple");
            }
        } else {
            System.out.println("Successful");
        }
    }

    private void move(ChannelHandlerContext ctx, Response response) throws Exception {
        if (!response.getErrorInfo().isSuccessful()) {
            if (response.getErrorInfo().isFileNotExists()) {
                System.out.println("Source file or directory not exists");
            } else if (response.getErrorInfo().isPathNotExists()) {
                System.out.println("Destination directory not exists");
            } else if (response.getErrorInfo().isWrongPath()) {
                System.out.println("You can't specify same paths or move directory to it's subdirectory ");
            } else if (response.getErrorInfo().isFileUnableToDelete()) {
                System.out.println("Unable to delete file");
            } else if (response.getErrorInfo().isFileLock()) {
                System.out.println("File has not been uploaded yet");
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
        } else if (response.getErrorInfo().isPathNotExists()) {
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
