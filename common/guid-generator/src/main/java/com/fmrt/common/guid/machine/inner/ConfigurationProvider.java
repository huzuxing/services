package com.fmrt.common.guid.machine.inner;

import com.fmrt.common.guid.machine.MachineProvider;
import com.fmrt.common.guid.machine.ProviderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Provider;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 21:58
 */
@Component
@Qualifier("configurationProvider")
@ConditionalOnProperty(prefix = "guid", value = "machineType", havingValue = "0")
public class ConfigurationProvider implements MachineProvider {

    @Autowired
    private ProviderType providerType;

    @Override
    public Mono<Long> getMachine() {
        return Mono.just(providerType.getMachineId());
    }
}
