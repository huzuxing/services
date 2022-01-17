package com.fmrt.common.guid.entity;
import com.fmrt.common.guid.machine.MachineProvider;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 21:11
 */
public enum MachineProviderEnum {
    PROPERTY(0, "配置文件"), DB(1, "数据库"), REDIS(2, "redis"), ZOOKEEPER(3, "zookeeper");

    MachineProviderEnum() {
    }

    MachineProviderEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    @Getter
    private int code;
    @Getter
    private String name;
    private final static IntFunction<MachineProviderEnum> CodeIndex;
    static {
        CodeIndex = codeIndex(MachineProviderEnum.values());
    }

    public static <T extends MachineProviderEnum> IntFunction<T> codeIndex(T[] values) {
        var m = new HashMap<Integer, T>(values.length);
        for (T v : values) {
            m.put(v.getCode(), v);
        }
        return code -> {
            var t = m.get(code);
            return Optional.of(t).orElseThrow();
        };
    }

    public static MachineProviderEnum ofCode(int code) {
        return CodeIndex.apply(code);
    }
}
