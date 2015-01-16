package pl.mcybulski.XMLConfig;

import pl.mcybulski.XMLConfig.exceptions.ImproperGrammaticalException;
import pl.mcybulski.XMLConfig.exceptions.UnrecognizedTokenException;
import pl.mcybulski.XMLConfig.exceptions.ValueParsingException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Mikołaj on 2014-12-21.
 */
public class Configuration {

    private Map<String, Object> configuration;

    public static Configuration getConfigurationFromXML(String path) {
        Parser parser = new Parser(path);

        Configuration configFromXML = null;
        try {
            configFromXML = parser.parseXML();
        } catch (ImproperGrammaticalException e) {
            e.printStackTrace();
        } catch (UnrecognizedTokenException e) {
            e.printStackTrace();
        } catch (ValueParsingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return configFromXML;
    }

    public Configuration() {
        configuration = new LinkedHashMap<String, Object>();
    }

    public void saveToXML(String path) {
        try {
            Serializer serializer = new Serializer(path);
            serializer.writeToXML(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void addParameter(String key, Object value) {
        configuration.put(key, value);
    }

    public void removeParameter(String key) {
        configuration.remove(key);
    }

    public Object getParameter(String key) {
        return configuration.get(key);
    }

    public void changeValue(String key, Object value) {
        configuration.replace(key, value);
    }
}
