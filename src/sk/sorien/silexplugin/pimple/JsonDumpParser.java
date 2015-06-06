package sk.sorien.silexplugin.pimple;

import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Stanislav Turza
 */
public class JsonDumpParser {

    @Nullable
    public static Container parse(File file) {
        try {
            Container container = new Container();

            FileReader reader = new FileReader(file);
            JSONParser jsonParser = new JSONParser();
            JSONArray elements = (JSONArray) jsonParser.parse(reader);

            for (Object element1 : elements) {
                JSONObject element = (JSONObject) element1;
                String name = element.get("name").toString();
                String type = element.get("type").toString();
                String value = element.get("value").toString();

                if (type.equals("class")) {
                    container.addService(new Service(name, value));
                } else {
                    container.addParameter(new Parameter(name, parameterFromString(type), value));
                }
            }

            return container;

        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    public static ParameterType parameterFromString(String value) {
        for (ParameterType p : ParameterType.values()) {
            if (value.equals(p.toString())) {
                return p;
            }
        }
        return ParameterType.UNKNOWN;
    }

}
