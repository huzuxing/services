package com.fmrt.common.guid.convertor;

import com.fmrt.common.guid.entity.Guid;
import com.fmrt.common.guid.entity.GuidMeta;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 21:15
 */
@Component
public class GuidConvertorImpl implements GuidConvertor{

    @Override
    public Mono<Long> convert(Guid guid, GuidMeta meta) {
        Objects.requireNonNull(guid);
        Objects.requireNonNull(meta);
        long ret = 0l;
        ret |= guid.getSequence();
        ret |= guid.getMachine() << meta.getMachineBitsStartPos();
        ret |= guid.getTime() << meta.getTimeBitsStartPos();
        return Mono.just(ret);
    }

    @Override
    public Mono<Guid> reconvert(long uid, GuidMeta meta) {
        Objects.requireNonNull(meta);
        var guid = new Guid();
        guid.setSequence(uid & meta.getSequenceBitMask());
        guid.setMachine(uid >>> meta.getMachineBitsStartPos() & meta.getMachineBitMask());
        guid.setTime(uid >>> meta.getTimeBitsStartPos() & meta.getTimeBitMask());
        return Mono.just(guid);
    }
}
