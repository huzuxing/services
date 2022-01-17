package com.fmrt.common.guid.entity;

import lombok.Data;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/11 20:55
 */
@Data
public class Guid implements java.io.Serializable {

    private static final long serialVersionUID = -9167775568349619528L;

    private long sequence;
    private long machine;
    private long time;
}
