package com.fmrt.common.guid.machine;

import com.fmrt.common.guid.entity.MachineProviderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 22:43
 */
@Service
@Qualifier("machineProvider")
public class MachineProviderProxy implements MachineProvider{

    @Autowired
    private ProviderType providerType;
    @Autowired
    @Qualifier("configurationProvider")
    private MachineProvider configurationProvider;

    // machine id 添加缓存，避免每次去查询数据库，或者程序启动后就把machine id 查出来放入内存
    private static Map<String, Long> MACHINE_CACHE = new ConcurrentHashMap<>();
    private final String MACHINE_CACHE_KEY = "machine_cache_key";

    @Override
    public Mono<Long> getMachine() {

        var machine = MACHINE_CACHE.get(MACHINE_CACHE_KEY);
        if (null != machine) {
            return Mono.just(machine);
        }
        var type = MachineProviderEnum.ofCode(providerType.getMachineType());
        // 可根据case不同，实现不同的machine id获取
        switch (type) {
            case PROPERTY:
                machine = configurationProvider.getMachine().block();
                break;
            default:
                throw new NoSuchElementException("no such machineid provider: " + providerType.getMachineType());
        }
        MACHINE_CACHE.put(MACHINE_CACHE_KEY, machine);
        return Mono.just(machine);
    }
}
