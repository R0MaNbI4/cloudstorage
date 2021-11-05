package ru.rompet.cloudstorage.server.domain.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.io.FileUtils;
import ru.rompet.cloudstorage.common.Response;
import ru.rompet.cloudstorage.common.Request;
import ru.rompet.cloudstorage.common.data.DirectoryStructure;
import ru.rompet.cloudstorage.common.enums.Parameter;
import ru.rompet.cloudstorage.server.dao.AuthenticationService;
import static ru.rompet.cloudstorage.common.IO.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;

public class RequestHandler extends SimpleChannelInboundHandler<Request> {
    private String rootDirectory;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
        switch (request.getCommand()) {
            case LOAD -> load(ctx, request);
            case SAVE -> save(ctx, request);
            case DELETE -> delete(ctx, request);
            case CREATE -> create(ctx, request);
            case DIR -> dir(ctx, request);
            case AUTH -> auth(ctx, request);
            case REGISTER -> register(ctx, request);
        }
    }

    private void load(ChannelHandlerContext ctx, Request request) throws Exception {
        Path path = Path.of(rootDirectory + request.getFromPath());
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                ctx.writeAndFlush(readPartFile(request, rootDirectory));
            } else if (Files.isDirectory(path)) {
                request.addToPaths("\\");
                List<Path> filePaths = DirectoryStructure.listFiles(request, rootDirectory, false); // relative path, because full destination path may not match full source path
                for (Path filePath : filePaths) {
                    Request request1 = (Request) request.clone();
                    request1.addToPaths(filePath.toString());
                    ctx.writeAndFlush(readPartFile(request1, rootDirectory));
                }
            }
        } else {
            Response response = new Response(request);
            response.getErrorInfo().setFileNotExists(true);
            ctx.writeAndFlush(response);
        }
    }

    private void save(ChannelHandlerContext ctx, Request request) throws Exception {
        Response response = new Response(request);
        if (Files.exists(Path.of(rootDirectory + request.getToPath()))
                && !(request.hasParameter(Parameter.RW) || request.hasParameter(Parameter.RN))
                && request.getPartFileInfo().isFirstPart()) {
            response.getErrorInfo().setFileAlreadyExists(true);
            ctx.writeAndFlush(response);
        } else {
            if (!request.hasData()) { // first initial request
                ctx.writeAndFlush(new Response(request));
                createParentDirectories(request, rootDirectory);
            } else {
                writePartFile(request, rootDirectory);
                if (!request.getPartFileInfo().isLastPart()) {
                    response.getPartFileInfo().addPosition(request, BUFFER_SIZE);
                    ctx.writeAndFlush(response);
                }
            }
        }
    }

    private void delete(ChannelHandlerContext ctx, Request request) throws Exception {
        Path path = Path.of(rootDirectory + request.getFromPath());
        File file = new File(rootDirectory + request.getFromPath());
        Response response = new Response(request);
        if (Files.exists(path)) {
            if (Files.isRegularFile(path)) {
                if (!file.delete()) {
                    response.getErrorInfo().setFileUnableToDelete(true);
                }
            } else if (Files.isDirectory(path)) {
                request.addToPaths("\\");
                List<Path> filePaths = DirectoryStructure.listFiles(request, rootDirectory, true);
                if (canDeleteFiles(filePaths)) {
                    deleteFiles(request, filePaths);
                } else {
                    response.getErrorInfo().setFileUnableToDelete(true);
                }
            }
        } else {
            response.getErrorInfo().setFileNotExists(true);
        }
        ctx.writeAndFlush(response);
    }

    private void create(ChannelHandlerContext ctx, Request request) throws Exception {
        Response response = new Response(request);
        Path path = Path.of(rootDirectory + request.getToPath());
        if (!Files.exists(path.getParent())) {
            response.getErrorInfo().setFileNotExists(true);
        } else {
            while (Files.exists(path)) {
                request.setToPath(rename(request.getToPath(), false));
                path = Path.of(rootDirectory + request.getToPath());
            }
            Files.createDirectory(Path.of(rootDirectory + request.getToPath()));
        }
        ctx.writeAndFlush(response);
    }

    private void dir(ChannelHandlerContext ctx, Request request) throws Exception {
        Response response = new Response(request);
        try {
            response.getDirectoryStructure().scan(rootDirectory + request.getFromPath());
        } catch (NoSuchFileException e) {
            response.getErrorInfo().setFileNotExists(true);
        }
        ctx.writeAndFlush(response);
    }

    private void auth(ChannelHandlerContext ctx, Request request) throws Exception {
        Response response = new Response(request);
        response.setAuthenticated(
                AuthenticationService.auth(
                        request.getCredentials().getLogin(),
                        request.getCredentials().getPassword()
                )
        );
        rootDirectory = request.getCredentials().getLogin() + "\\";
        if (!Files.exists(Path.of(rootDirectory))) {
            Files.createDirectory(Path.of(rootDirectory));
        }
        ctx.writeAndFlush(response);
    }

    private void register(ChannelHandlerContext ctx, Request request) {
        Response response = new Response(request);
        response.getErrorInfo().setSuccessful(AuthenticationService.register(
                request.getCredentials().getLogin(),
                request.getCredentials().getPassword()
        ));
        ctx.writeAndFlush(response);
    }

    private boolean canDeleteFile(String path) {
        File file = new File(path);
        return file.renameTo(file);
    }

    private boolean canDeleteFiles(List<Path> filePaths) {
        for (Path filePath : filePaths) {
            if (!canDeleteFile(filePath.toString())) {
                return false;
            }
        }
        return true;
    }

    private void deleteFiles(Request request, List<Path> filePaths) throws IOException {
        if (request.hasParameter(Parameter.R)) {
            FileUtils.deleteDirectory(new File(rootDirectory + request.getFromPath()));
        } else {
            for (Path filePath : filePaths) {
                Files.delete(filePath);
            }
        }
    }
}
