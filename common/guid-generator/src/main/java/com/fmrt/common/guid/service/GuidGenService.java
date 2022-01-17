package com.fmrt.common.guid.service;

import com.fmrt.common.guid.entity.Guid;
import reactor.core.publisher.Mono;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 22:22
 */
public interface GuidGenService {
    Mono<Long> gen() throws Exception;
    Mono<Guid> expand(Long guid);
}
