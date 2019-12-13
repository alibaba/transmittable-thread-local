package com.alibaba.demo.hystrix;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalUtil {

    private static ThreadLocal<ConcurrentHashMap<String, Object>> transmittableThreadLocal = new ThreadLocal<>();

    public static ConcurrentHashMap<String, Object> getThreadLocalData() {
        return transmittableThreadLocal.get();
    }

    public static void setThreadLocalData(ConcurrentHashMap<String, Object> data) {
        transmittableThreadLocal.set(data);
    }

    public static void addDataToThreadLocalMap(String key, Object value) {
        Map<String, Object> existingDataMap = transmittableThreadLocal.get();
        if (value != null) {
            existingDataMap.put(key, value);
        }
    }

    public static Object getDataFromThreadLocalMap(String key) {
        Map<String, Object> existingDataMap = transmittableThreadLocal.get();
        return existingDataMap.get(key);
    }

    public static void clearThreadLocalDataMap() {
        if (transmittableThreadLocal != null) 
            transmittableThreadLocal.remove();
    }

    public static Object getRequestData(String key) {
        Map<String, Object> existingDataMap = transmittableThreadLocal.get();
        if (existingDataMap != null) {
            return existingDataMap.get(key);
        }
        return "-1";
    }



    public static void initThreadLocals() {
        transmittableThreadLocal.set(new ConcurrentHashMap<>());
        String requestId = "REQUEST_ID_" + System.currentTimeMillis();
        addDataToThreadLocalMap("REQUEST_ID", requestId);
    }
}
