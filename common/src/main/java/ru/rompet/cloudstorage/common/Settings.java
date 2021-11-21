package ru.rompet.cloudstorage.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class Settings {
    private static final String SETTINGS_DIRECTORY = ".cloudstorage";
    public static final String SETTINGS_FILE = "properties.properties";

    public static boolean setRoot(String path) throws Exception {
        return Settings.setSetting("root", path);
    }

    public static String getRoot() throws Exception {
        return Settings.getSetting("root");
    }

    private static Path getSettingsDirectory() {
        String userHome = System.getProperty("user.home");
        if(userHome == null) {
            throw new IllegalStateException("user.home==null");
        }
        File home = new File(userHome);
        File settingsDirectory = new File(home, SETTINGS_DIRECTORY);
        if(!settingsDirectory.exists()) {
            if(!settingsDirectory.mkdir()) {
                throw new IllegalStateException(settingsDirectory.toString());
            }
        }
        return Path.of(settingsDirectory.getAbsolutePath());
    }

    private static File getSettingsFile() throws Exception {
        File settingsFile = new File(getSettingsDirectory() + SETTINGS_FILE);
        if (!settingsFile.exists()) {
            if (!settingsFile.createNewFile()) {
                System.out.println("Failed to create properties file");
            }
        }
        return settingsFile;
    }

    private static boolean setSetting (String key, String value) throws Exception {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(getSettingsFile()));
            properties.setProperty(key, value);
            properties.store(new FileOutputStream(getSettingsFile()), null);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String getSetting (String key) throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream(getSettingsFile()));
        return properties.getProperty(key);
    }
}
