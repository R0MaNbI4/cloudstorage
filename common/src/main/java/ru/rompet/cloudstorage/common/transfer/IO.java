package ru.rompet.cloudstorage.common.transfer;

import java.io.RandomAccessFile;

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
}
