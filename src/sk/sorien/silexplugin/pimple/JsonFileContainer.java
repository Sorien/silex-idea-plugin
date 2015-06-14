package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class JsonFileContainer extends Container {

    private static final String CONTAINER_JSON_DUMP = "pimple.json";

    private long lastModified = 0;
    private final File file;

    public JsonFileContainer(Project project) {
        super(project);
        file = new File(project.getBaseDir().getPath() + '/' + CONTAINER_JSON_DUMP);
    }

    @Override
    public Map<String, Service> getServices() {
        Load();
        return super.getServices();
    }

    @Override
    public Map<String, Parameter> getParameters() {
        Load();
        return super.getParameters();
    }

    private void Load() {

        if (file.exists() && file.lastModified() != lastModified && parse()) {
            lastModified = file.lastModified();
        }
    }

    private Boolean parse() {
        try {
            FileReader reader = new FileReader(file);
            JSONParser jsonParser = new JSONParser();
            JSONArray elements = (JSONArray) jsonParser.parse(reader);

            services.clear();
            parameters.clear();

            for (Object element1 : elements) {
                JSONObject element = (JSONObject) element1;
                String name = element.get("name").toString();
                String type = element.get("type").toString();
                String value = element.get("value").toString();

                if (type.equals("class")) {
                    services.put(name, (new Service(name, value)));
                } else {
                    parameters.put(name, new Parameter(name, parameterFromString(type), value));
                }
            }

            return true;

        } catch (FileNotFoundException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        } catch (NullPointerException ex) {
            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    private static ParameterType parameterFromString(String value) {
        for (ParameterType p : ParameterType.values()) {
            if (value.equals(p.toString())) {
                return p;
            }
        }
        return ParameterType.UNKNOWN;
    }
}
