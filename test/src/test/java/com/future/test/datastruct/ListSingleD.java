package com.future.test.datastruct;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/10 10:31
 */
public class ListSingleD<T> {
    private NodeD<T> head = null;

    public ListSingleD() {
    }

    public boolean add(T v) {
        var node = new NodeD<T>(v);
        if (null == head) {
            head = node;
            return true;
        }
        node.next = head.next;
        head.next = node;
        return true;
    }
    public void list() {
        var node = head;
        while (null != node) {
            System.out.println(node.data);
            node = node.next;
        }
    }

    public static void main(String[] args) {
        var ld = new ListSingleD();
        ld.add(1);
        ld.add(2);
        ld.add(3);
        ld.add(4);
        ld.add(5);
        ld.add(6);
        ld.list();
        BlockingQueue queue = new ArrayBlockingQueue(500);
    }

    class NodeD<T> {
        private T data;
        private NodeD<T> next;

        public NodeD(T data) {
            this.data = data;
        }
    }
}

