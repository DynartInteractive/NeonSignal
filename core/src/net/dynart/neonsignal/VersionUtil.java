package net.dynart.neonsignal;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionUtil {
    private static String version = "unknown";

    static {
        try (InputStream input = VersionUtil.class.getClassLoader().getResourceAsStream("version.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                version = prop.getProperty("version", version);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getVersion() {
        return version;
    }
}