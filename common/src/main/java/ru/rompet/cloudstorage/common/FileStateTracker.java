package ru.rompet.cloudstorage.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileStateTracker {
    private final ArrayList<String> listOfPaths = new ArrayList<>();

    public void sync(Message message, String rootDirectory) {
        if (message.getPartFileInfo().isFirstPart()) {
            listOfPaths.add(rootDirectory + message.getToPath());
        }
        if (message.getPartFileInfo().isLastPart()) {
            listOfPaths.remove(rootDirectory + message.getToPath());
        }
    }

    public boolean isLock(Message message, String rootDirectory, boolean isFile) {
        if (listOfPaths.size() > 0) {
            if (isFile) {
                return listOfPaths.contains(rootDirectory + message.getFromPath());
            } else {
                for (String path : listOfPaths) {
                    if (path.startsWith(rootDirectory + message.getFromPath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getCount() {
        return listOfPaths.size();
    }

    public void deletePartiallyDownloadedFiles() throws IOException {
        for (String filePath : listOfPaths) {
            Files.delete(Path.of(filePath));
        }
    }
}
