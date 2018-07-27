package com.alibaba.ttl.classloader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//c
public class TtlClassCache<T> {

    private Map<String, Map<ClassLoader, T>> classCacheMap = new ConcurrentHashMap<String, Map<ClassLoader, T>>();

    public T loadClassByName(ClassLoader paramClassLoader, String className) {
        Map<ClassLoader, T> classLoaderKeyAndClassValueMap = (Map<ClassLoader, T>) this.classCacheMap.get(className);
        if (classLoaderKeyAndClassValueMap != null) {
            if (classLoaderKeyAndClassValueMap.containsKey(paramClassLoader)) {
                return (T) classLoaderKeyAndClassValueMap.get(paramClassLoader);
            }
            ClassLoader parentClassLoader = paramClassLoader == null ? null : paramClassLoader.getParent();
            while ((parentClassLoader != null) && (!parentClassLoader.getClass().equals(ClassLoader.class))) {
                if (classLoaderKeyAndClassValueMap.containsKey(parentClassLoader)) {
                    return (T) classLoaderKeyAndClassValueMap.get(parentClassLoader);
                }
                if (parentClassLoader.equals(paramClassLoader.getParent())) {
                    break;
                }
                parentClassLoader = paramClassLoader.getParent();
            }
        }
        return null;
    }

    public void setClazzLoaderMap(ClassLoader paramClassLoader, String className, T clazz) {
        if (!this.classCacheMap.containsKey(className)) {
            this.classCacheMap.put(className, new HashMap<ClassLoader, T>());
        }
        ((Map<ClassLoader, T>) this.classCacheMap.get(className)).put(paramClassLoader, clazz);
    }

}
