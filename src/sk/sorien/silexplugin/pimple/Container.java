package sk.sorien.silexplugin.pimple;

import com.intellij.openapi.project.Project;
import sk.sorien.silexplugin.utils.ContainerMap;
import sk.sorien.silexplugin.utils.ContainerMapItem;

/**
 * @author Stanislav Turza
 */
public class Container extends ContainerMapItem {

    protected final ContainerMap<Service> services = new ContainerMap<Service>();
    protected final ContainerMap<Parameter> parameters = new ContainerMap<Parameter>();
    protected final ContainerMap<Container> containers = new ContainerMap<Container>();
    protected final Project project;

    public Container(Project project) {
        this("_unknown_", project);
    }

    public Container(String name, Project project) {
        super(name);
        this.project = project;
    }

    public ContainerMap<Service> getServices() {
        return services;
    }

    public ContainerMap<Parameter> getParameters() {
        return parameters;
    }

    public ContainerMap<Container> getContainers() {
        return containers;
    }

    public Container put(Service value) {
        this.getServices().put(value);
        return this;
    }

    public Container put(Parameter value) {
        this.getParameters().put(value);
        return this;
    }

    public Container put(Container value) {
        this.getContainers().put(value);
        return this;
    }
}

