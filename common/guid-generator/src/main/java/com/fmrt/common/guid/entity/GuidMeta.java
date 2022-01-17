package com.fmrt.common.guid.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 21:04
 */
@Data
@AllArgsConstructor
public class GuidMeta {
    private long sequenceBits;
    private long machineBits;
    private long timeBits;

    public long getSequenceBitsStartPos() {
        return 0l;
    }

    public long getMachineBitsStartPos() {
        return this.sequenceBits;
    }

    public long getTimeBitsStartPos() {
        return this.sequenceBits + this.machineBits;
    }

    public long getSequenceBitMask() {
        return -1l ^ (-1l << this.sequenceBits);
    }

    public long getMachineBitMask() {
        return -1l ^ (-1l << this.machineBits);
    }

    public long getTimeBitMask() {
        return -1l ^ (-1l << this.timeBits);
    }
}
