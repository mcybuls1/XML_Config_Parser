package pl.mcybulski.XMLConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Mikołaj on 2014-12-27.
 */
public class Serializer {

    private FileWriter fileWriter;
    private String prefix;

    public Serializer(String path) throws IOException {
        File xml = new File(path);
        xml.createNewFile();
        fileWriter = new FileWriter(xml);
        prefix = "";
    }

    public void writeToXML(Configuration configuration) throws Exception {
        fileWriter.write("<?xml version=\"1.0\"?>\n");
        writeConfiguration(configuration, "Config");
        fileWriter.flush();
        fileWriter.close();
    }

    private void writeConfiguration(Configuration configuration, String name) throws Exception {
        fileWriter.append(prefix + "<" + name + ">\n");
        prefix += "\t";

        for (Map.Entry<String, Object> entry : configuration.getConfiguration().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Configuration) {
                writeConfiguration((Configuration)value, key);
            }
            else if (value instanceof ArrayList) {
                writeArray((List<Object>)value, key);
            }
            else {
                writeElement(value, key);
            }
        }

        prefix = prefix.substring(1);
        fileWriter.append(prefix + "</" + name + ">\n");
    }

    private void writeElement(Object value, String name) throws Exception {
        fileWriter.append(prefix + "<" + name + " type=\"");

        try {
            appendType(value);
        } catch (Exception e) {
            throw new Exception(e.getMessage() + " w elemencie " + name);
        }

        if (value != null) {
            fileWriter.append("\">" + value.toString() + "</" + name + ">\n");
        }
        else {
            fileWriter.append("\">" + "</" + name + ">\n");
        }
    }

    private void writeArray(List<Object> array, String name) throws Exception {
        fileWriter.append(prefix + "<" + name + " array=\"");

        try {
            appendType(array.get(0));
        } catch (Exception e) {
            throw new Exception(e.getMessage() + " w tablicy " + name);
        }

        fileWriter.append("\">\n");
        prefix += "\t";

        for (Object o : array) {
            fileWriter.append(prefix + "<value>" + o.toString() + "</value>\n");
        }

        prefix = prefix.substring(1);
        fileWriter.append(prefix + "</" + name + ">\n");
    }

    private void appendType(Object value) throws Exception {
        if (value instanceof String) {
            fileWriter.append("string");
        }
        else if (value == null) {
            fileWriter.append("null");
        }
        else if (value instanceof Double) {
            fileWriter.append("double");
        }
        else if (value instanceof Long) {
            fileWriter.append("long");
        }
        else if (value instanceof Boolean) {
            fileWriter.append("boolean");
        }
        else if (value instanceof Float) {
            fileWriter.append("float");
        }
        else if (value instanceof Integer) {
            fileWriter.append("int");
        }
        else {
            throw new Exception("Niewspierany typ obiektu");
        }
    }
}
