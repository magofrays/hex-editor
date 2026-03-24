package hex.editor.config;

import hex.editor.exception.FileException;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class HexEditorConfig {
    private static HexEditorConfig instance;
    private final Properties properties = new Properties();
    public HexEditorConfig() {
        String configHome = System.getenv("XDG_CONFIG_HOME");
        if (configHome == null) {
            configHome = System.getProperty("user.home") + "/.config";
        }
        Path userConfigFile = Paths.get(configHome, "hex-editor/config.properties");
        if (Files.exists(userConfigFile)) {
            try {
                properties.load(new FileReader(userConfigFile.toFile()));
            } catch (IOException e) {
                throw new FileException("Failed to load file: " + userConfigFile, e);
            }
        }
        Path configSystem = Paths.get("/etc/hex-editor/config.properties");
        if(Files.exists(configSystem)) {
            try {
                properties.load(new FileReader(configSystem.toFile()));
            } catch (IOException e) {
                throw new FileException("Failed to load file: " + userConfigFile, e);
            }
        }
        InputStream is = getClass().getResourceAsStream("/config/default.properties");
        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                throw new FileException("Failed to load file: " + is, e);
            }
        }
    }

    public static HexEditorConfig getInstance() {
        return instance;
    }

    public String getString(String key) {
        return properties.getProperty(key);
    }
    public Long getLong(String key) {
        return Long.parseLong(properties.getProperty(key));
    }
    public Integer getInteger(String key) {
        return Integer.parseInt(properties.getProperty(key));
    }

}
