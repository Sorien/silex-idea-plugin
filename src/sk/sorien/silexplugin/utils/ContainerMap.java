package sk.sorien.silexplugin.utils;

import com.intellij.util.containers.HashMap;

public class ContainerMap<T extends ContainerMapItem> extends HashMap<String, T> {

    @Override
    @Deprecated
    public T put(String key, T value) {
        return super.put(value.getName(), value);
    }

    public T put(T value) {
        return super.put(value.getName(), value);
    }
}
