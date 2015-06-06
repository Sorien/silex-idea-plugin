package sk.sorien.silexplugin.pimple;

import com.intellij.util.containers.HashMap;

import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class Container {

    private Map<String, Service> services;
    private Map<String, Parameter> parameters;

    public Container() {
        services = new HashMap<String, Service>();
        parameters = new HashMap<String, Parameter>();
    }

    public Map<String, Service> getServices() {
        return services;
    }

    public void setServices(Map<String, Service> services) {
        this.services = services;
    }

    public void addService(Service service) {
        services.put(service.getName(), service);
    }

    public Map<String, Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Parameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(Parameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }
}
