package com.fmrt.common.guid.service.impl;

import com.fmrt.common.guid.convertor.GuidConvertor;
import com.fmrt.common.guid.entity.Guid;
import com.fmrt.common.guid.entity.GuidMeta;
import com.fmrt.common.guid.machine.MachineProvider;
import com.fmrt.common.guid.service.GuidGenService;
import com.fmrt.common.guid.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 22:28
 */
@Service
public class GuidGenServiceImpl implements GuidGenService {

    private long sequence = 0l;

    private GuidMeta meta = new GuidMeta(12, 10, 41);

    private final ReentrantLock lock = new ReentrantLock();

    private long lastTimestamp = -1l;

    @Autowired
    @Qualifier("machineProvider")
    private MachineProvider machineProvider;

    @Autowired
    private GuidConvertor guidConvertor;

    @Override
    public Mono<Long> gen() throws Exception {
        var machine = machineProvider.getMachine().block();
        var guid = new Guid();
        guid.setMachine(machine);
        lock.lock();
        try {
            long timeStamp = TimeUtils.getTime();
            TimeUtils.validateTimestamp(lastTimestamp, timeStamp);
            if (timeStamp == lastTimestamp) {
                sequence = (sequence + 1) & meta.getSequenceBitMask();
                if (0 == sequence) {
                    timeStamp = TimeUtils.tillNextTime(lastTimestamp);
                }
            }
            else {
                sequence = 0;
            }
            lastTimestamp = timeStamp;
            guid.setSequence(sequence);
            guid.setTime(timeStamp);
            return guidConvertor.convert(guid, meta);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (lock.isLocked()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Mono<Guid> expand(Long guid) {
        return guidConvertor.reconvert(guid, meta);
    }
}
