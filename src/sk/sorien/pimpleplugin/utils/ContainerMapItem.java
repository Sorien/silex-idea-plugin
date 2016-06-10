package sk.sorien.pimpleplugin.utils;

public abstract class ContainerMapItem {

    private final String name;

    protected ContainerMapItem(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
