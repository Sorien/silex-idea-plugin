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
    private final JSONParser jsonParser = new JSONParser();

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

    @Override
    public Map<String, Container> getContainers() {
        Load();
        return super.getContainers();
    }

    private void Load() {

        if (file.exists() && file.lastModified() != lastModified && parse()) {
            lastModified = file.lastModified();
        }
    }

    private boolean parseContainer(Container container, JSONArray elements) {

        container.services.clear();
        container.parameters.clear();
        container.containers.clear();

        for (Object element1 : elements) {
            JSONObject element = (JSONObject) element1;
            String name = element.get("name").toString();
            String type = element.get("type").toString();
            String value = element.get("value").toString();

            if (type.equals("class")) {
                container.services.put(name, (new Service(name, value)));
            }
            else if (type.equals("container")) {

                Container subContainer = new Container(project);
                try {
                    parseContainer(subContainer, (JSONArray) jsonParser.parse(value));
                } catch (ParseException e) {
                    return false;
                }

                container.containers.put(name, subContainer);
            }
            else {
                container.parameters.put(name, new Parameter(name, parameterFromString(type), value));
            }
        }

        return true;
    }

    private Boolean parse() {
        try {
            FileReader reader = new FileReader(file);
            return parseContainer(this, (JSONArray) jsonParser.parse(reader));

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
