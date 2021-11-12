package ru.rompet.cloudstorage.common;

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
        if (isFile) {
            return listOfPaths.contains(rootDirectory + message.getFromPath());
        } else {
            for (String path : listOfPaths) {
                if (path.startsWith(rootDirectory + message.getFromPath())) {
                    return true;
                }
            }
        }
        return false;
    }
}
