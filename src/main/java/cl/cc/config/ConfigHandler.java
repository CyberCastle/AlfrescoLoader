package cl.cc.config;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author CyberCastle
 */
public final class ConfigHandler {

    private static Properties configFile = null;

    public static String getProperty(String keyName) {
        String response = "";
        try {
            if (configFile == null) {
                reloadConfigFile();
            }
            response = configFile.getProperty(keyName);
            if (response.contains("${")) {
                int posOne = response.indexOf("${");
                int posTwo = response.indexOf("}");
                String subParameterName = response.substring(posOne + 2, posTwo);
                String subParameterValue = configFile.getProperty(subParameterName);
                response = response.replace("${" + subParameterName + "}", subParameterValue);
            }
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
        return response;
    }

    public static void reloadConfigFile() throws IOException {
        configFile = null;
        configFile = new Properties();
        configFile.load(new ConfigHandler().getClass().getResourceAsStream("settings.properties"));
    }

    public static Properties getConfigProperties() {
        return configFile;
    }
}