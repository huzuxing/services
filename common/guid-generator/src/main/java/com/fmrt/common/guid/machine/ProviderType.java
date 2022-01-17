package com.fmrt.common.guid.machine;

import com.fmrt.common.guid.entity.MachineProviderEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 22:06
 */
@Component
@ConfigurationProperties(prefix = "guid")
@Getter
@Setter
public class ProviderType {
    private Integer machineType = MachineProviderEnum.PROPERTY.getCode();
    private Long machineId = 0l;
}
