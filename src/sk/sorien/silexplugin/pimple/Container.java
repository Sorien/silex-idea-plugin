package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.HashMap;

import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class Container {

    protected final Map<String, Service> services = new HashMap<String, Service>();
    protected final Map<String, Parameter> parameters = new HashMap<String, Parameter>();
    protected final Map<String, Container> containers = new HashMap<String, Container>();
    protected final Project project;

    public Container(Project project) {
        this.project = project;
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public Map<String, Container> getContainers() {
        return containers;
    }
}

