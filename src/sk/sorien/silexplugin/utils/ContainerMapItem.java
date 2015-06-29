package sk.sorien.silexplugin.utils;

public abstract class ContainerMapItem {

    private String name;

    protected ContainerMapItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
