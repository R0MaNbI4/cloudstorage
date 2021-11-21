package ru.rompet.cloudstorage.common;

import ru.rompet.cloudstorage.common.enums.Parameter;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

public class IO {
    public static final int BUFFER_SIZE = 1024 * 512;

    public static void writePartFile(Message message, String rootDirectory) throws Exception {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(
                rootDirectory + message.getToPath(), "rw")) {
            randomAccessFile.seek(message.getPartFileInfo().getPosition());
            randomAccessFile.write(message.getPartFileInfo().getFile());
        }
    }

    public static Message readPartFile(Message message, String rootDirectory) throws Exception {
        Message message1;
        if (message instanceof Response) {
            message1 = new Request((Response) message);
        } else {
            message1 = new Response((Request) message);
        }
        message1.getPartFileInfo().setPosition(message);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (RandomAccessFile accessFile = new RandomAccessFile(
                rootDirectory + message.getFromPath(), "r")) {
            accessFile.seek(message.getPartFileInfo().getPosition());
            int read = accessFile.read(buffer);
            if (read == -1) { // if the file size is 0 or a multiple of the buffer size
                message1.getPartFileInfo().setFile(new byte[0]);
                message1.getPartFileInfo().setLastPart(true);
            } else if (read < buffer.length - 1) {
                byte[] tempBuffer = new byte[read];
                System.arraycopy(buffer, 0, tempBuffer, 0, read);
                message1.getPartFileInfo().setFile(tempBuffer);
                message1.getPartFileInfo().setLastPart(true);
            } else {
                message1.getPartFileInfo().setFile(buffer);
                message1.getPartFileInfo().setLastPart(false);
            }
        }
        return message1;
    }

    public static void createParentDirectories(Message message, String rootDirectory) throws Exception {
        Path path = Path.of(rootDirectory + message.getToPath()).getParent();
        if (Files.notExists(path)) {
            Files.createDirectories(path);
        }
    }

    public static boolean isPathExists(Message message, String rootDirectory) throws Exception {
        Path path = Path.of(rootDirectory + message.getToPath()).getParent();
        return Files.exists(path);
    }

    public static String rename(String name, boolean isFile) {
        StringBuilder sb = new StringBuilder();
        int number;
        boolean hasNumber = isFile && name.matches("^.+\\s\\(\\d+\\)\\..+$"); // check number if is file
        hasNumber = hasNumber || name.matches("^.+\\s\\(\\d+\\)$"); // check number anyway
        boolean hasExtension = isFile && name.matches("^.+\\..+$");
        number = hasNumber ? incrementNameNumber(getNameNumber(name, hasExtension)) : 1;
        sb.append(getNameWithoutNumber(name, hasNumber, hasExtension))
                .append(" (").append(number).append(")")
                .append(getExtension(name, hasExtension));
        return sb.toString();
    }

    private static int getNameNumber(String name, boolean hasExtension) {
        String number;
        if (hasExtension) {
            number = name.substring(name.lastIndexOf(" ") + 1, name.lastIndexOf(".") - 1);
        } else {
            number = name.substring(name.lastIndexOf(" ") + 1);
        }
        number = number.replaceAll("[(,)]","");
        return Integer.parseInt(number);
    }

    private static int incrementNameNumber(int number) {
        if (number == Integer.MAX_VALUE) {
            return 1;
        } else {
            return ++number;
        }
    }

    private static String getNameWithoutNumber(String name, boolean hasNumber, boolean hasExtension) {
        if (hasNumber) {
            return name.substring(0, name.lastIndexOf(" ")); // test (1) or test (1).txt
        } else {
            if (hasExtension) {
                return name.substring(0, name.lastIndexOf(".")); // test.txt
            } else {
                return name; // test
            }
        }
    }

    private static String getExtension(String name, boolean hasExtension) {
        return hasExtension ? name.substring(name.lastIndexOf(".")) : "";
    }
}
