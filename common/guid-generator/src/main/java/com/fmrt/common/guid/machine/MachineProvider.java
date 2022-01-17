package com.fmrt.common.guid.machine;

import reactor.core.publisher.Mono;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 21:54
 */
public interface MachineProvider {
    Mono<Long> getMachine();
}
