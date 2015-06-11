package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import com.intellij.util.containers.HashMap;

import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class Container {

    protected Map<String, Service> services;
    protected Map<String, Parameter> parameters;

    protected Project project;

    public Container(Project project) {
        this.project = project;
        services = new HashMap<String, Service>();
        parameters = new HashMap<String, Parameter>();
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }
}

