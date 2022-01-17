package com.future.common.redis.manager;

import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.KeyValue;
import io.lettuce.core.Value;
import io.lettuce.core.api.StatefulConnection;
import reactor.core.publisher.Mono;
import reactor.pool.PooledRef;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class RedisUtil {
    private RedisUtil() {
    }

    static final ClassLoader classLoader = AbstractRedisClient.class.getClassLoader();

    private static final Constructor<? extends InvocationHandler> InvocationHandlerConstructor =
            AccessController.doPrivileged((PrivilegedAction<Constructor<? extends InvocationHandler>>) () -> {
                try {
                    @SuppressWarnings("unchecked")
                    var cls = (Class<? extends InvocationHandler>) Class.forName("io.lettuce.core.FutureSyncInvocationHandler");
                    var c = cls.getDeclaredConstructor(StatefulConnection.class, Object.class, Class[].class);
                    c.setAccessible(true);
                    try {
                        c.newInstance(null, null, null);
                    } catch (InvocationTargetException | IllegalArgumentException | NullPointerException ignored) {
                    }
                    return c;
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            });

    @SuppressWarnings("unchecked")
    static <T> T syncHandler(StatefulConnection<?, ?> connection, Object asyncApi, Class<?>... interfaces) {
        try {
            var h = InvocationHandlerConstructor.newInstance(connection, asyncApi, interfaces);
            return (T) Proxy.newProxyInstance(classLoader, interfaces, h);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new IllegalStateException(e);
        }
    }
    //

    static <T> T unSupportMethod() {
        throw new UnsupportedOperationException();
    }

    static <T> Function<PooledRef<T>, Mono<Void>> poolRelease() {
        return PooledRef::release;
    }

    public static <K, V> Function<KeyValue<K, V>, K> getKey() {
        return KeyValue::getKey;
    }

    public static <K, V> List<V> toValues(List<KeyValue<K, V>> keyValues) {
        return toValues(keyValues, Function.identity());
    }

    public static <K, V, R> List<R> toValues(List<KeyValue<K, V>> keyValues, Function<V, R> mapper) {
        if (null == keyValues || keyValues.isEmpty()) {
            return new ArrayList<>();
        }
        var li = new ArrayList<R>(keyValues.size());
        for (var kv : keyValues) {
            if (kv.hasValue()) {
                li.add(mapper.apply(kv.getValue()));
            }
        }
        return li;
    }

    public static <K, V> Function<List<KeyValue<K, V>>, List<V>> toValuesFun() {
        return RedisUtil::toValues;
    }

    public static <K, V> Map<K, V> toMap(List<KeyValue<K, V>> keyValues) {
        return toMap(keyValues, Function.identity());
    }

    public static <K, V, MK> Map<MK, V> toMap(List<KeyValue<K, V>> keyValues,
                                              Function<K, MK> keyMap) {
        return toMap(keyValues, keyMap, Function.identity());
    }

    public static <K, V, MK, MV> Map<MK, MV> toMap(List<KeyValue<K, V>> keyValues,
                                                   Function<K, MK> keyMap,
                                                   Function<V, MV> valMap) {
        var m = new HashMap<MK, MV>(keyValues.size());
        for (var kv : keyValues) {
            if (kv.hasValue()) {
                m.put(keyMap.apply(kv.getKey()), valMap.apply(kv.getValue()));
            }
        }
        return m;
    }

    public static <K, V> Function<List<KeyValue<K, V>>, Map<K, V>> toMapFun() {
        return RedisUtil::toMap;
    }


    public static <K, V> Collector<KeyValue<K, V>, ?, Map<K, V>> toMap() {
        return Collectors.toMap(KeyValue::getKey, Value::getValue);
    }

    public static <K, V> Predicate<KeyValue<K, V>> noNull() {
        return Value::hasValue;
    }

}
