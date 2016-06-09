package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import sk.sorien.silexplugin.SilexProjectComponent;
import sk.sorien.silexplugin.utils.ContainerMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author Stanislav Turza
 */
public class JsonFileContainer extends Container {

    private static final String CONTAINER_JSON_DUMP = "pimple.json";
    private static final String CONTAINER_LOAD_ERROR = "Failed to load container definitions from " + CONTAINER_JSON_DUMP;

    private long lastModified = 0;
    private final File file;
    private final JSONParser jsonParser = new JSONParser();

    public JsonFileContainer(Project project) {
        super(project);
        file = new File(project.getBaseDir().getPath() + '/' + CONTAINER_JSON_DUMP);

        if (!file.exists()) {
            SilexProjectComponent.warning(CONTAINER_LOAD_ERROR, project);
        }
    }

    @Override
    public ContainerMap<Service> getServices() {
        Load();
        return super.getServices();
    }

    @Override
    public ContainerMap<Parameter> getParameters() {
        Load();
        return super.getParameters();
    }

    @Override
    public ContainerMap<Container> getContainers() {
        Load();
        return super.getContainers();
    }

    private void Load() {

        if (file.exists() && file.lastModified() != lastModified) {
            parse();
        }
    }

    private void parseContainer(Container container, JSONArray elements) throws ParseException {

        container.services.clear();
        container.parameters.clear();
        container.containers.clear();

        for (Object element1 : elements) {
            JSONObject element = (JSONObject) element1;
            String name = element.get("name").toString();
            String type = element.get("type").toString();
            String value = element.get("value").toString();

            if (type.equals("class")) {
                container.put(new Service(name, value));
            }
            else if (type.equals("container")) {

                Container subContainer = new Container(name, project);
                parseContainer(subContainer, (JSONArray) jsonParser.parse(value));

                container.put(subContainer);
            }
            else {
                container.put(new Parameter(name, parameterFromString(type), value));
            }
        }
    }

     private synchronized void parse() {
        try {
            lastModified = file.lastModified();
            parseContainer(this, (JSONArray) jsonParser.parse(new FileReader(file)));

        } catch (FileNotFoundException e) {
            SilexProjectComponent.error(CONTAINER_LOAD_ERROR, project);
        } catch (IOException e) {
            SilexProjectComponent.error(CONTAINER_LOAD_ERROR, project);
        } catch (NullPointerException e) {
            SilexProjectComponent.error(CONTAINER_LOAD_ERROR, project);
        } catch (ParseException e) {
            SilexProjectComponent.error(CONTAINER_LOAD_ERROR, project);
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
