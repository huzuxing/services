package com.fmrt.common.guid.convertor;

import com.fmrt.common.guid.entity.Guid;
import com.fmrt.common.guid.entity.GuidMeta;
import reactor.core.publisher.Mono;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 21:13
 */
public interface GuidConvertor {
    Mono<Long> convert(Guid guid, GuidMeta meta);
    Mono<Guid> reconvert(long uid, GuidMeta meta);
}
