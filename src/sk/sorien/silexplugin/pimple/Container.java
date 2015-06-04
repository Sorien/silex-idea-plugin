package sk.sorien.silexplugin.pimple;

import com.intellij.util.containers.HashMap;

import java.util.Map;

/**
 * @author Stanislav Turza
 */
public class Container {

    private Map<String, ContainerService> services;
    private Map<String, ContainerParameter> parameters;

    public Container() {
        services = new HashMap<String, ContainerService>();
        parameters = new HashMap<String, ContainerParameter>();
    }

    public Map<String, ContainerService> getServices() {
        return services;
    }

    public void setServices(Map<String, ContainerService> services) {
        this.services = services;
    }

    public void addService(ContainerService service) {
        services.put(service.getName(), service);
    }

    public Map<String, ContainerParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ContainerParameter> parameters) {
        this.parameters = parameters;
    }

    public void addParameter(ContainerParameter parameter) {
        parameters.put(parameter.getName(), parameter);
    }
}
