package com.future.concurrent.thread;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.*;
import java.text.MessageFormat;
import java.util.concurrent.*;

/**
 * Created by huzuxing on 2021/5/28.
 */
@Slf4j
public class RejectedPolicyWithReport implements RejectedExecutionHandler {

    private static volatile long lastPrintTime = 0;

    private static final long TEN_MINUTES_MILLS = 10 * 60 * 1000;

    private static Semaphore guard = new Semaphore(1);
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        try {
            String title = "thread pool execute reject policy!!";
            String msg = MessageFormat.format(
                    "{0}{1}core pool size:{2}, current pool size:{3}, queue wait size:{4}, active count:{5}, completed task count:{6}, " +
                            "task count:{7}, largest pool size:{8}, max pool size:{9}, keep alive time:{10}, is shutdown:{11}, is terminated:{12}, " +
                            "thread name:{13}{14}",
                    System.lineSeparator(), title, e.getCorePoolSize(), e.getPoolSize(), e.getQueue().size(), e.getActiveCount(),
                    e.getCompletedTaskCount(), e.getTaskCount(), e.getLargestPoolSize(), e.getMaximumPoolSize(), e.getKeepAliveTime(TimeUnit.SECONDS),
                    e.isShutdown(), e.isTerminated(), Thread.currentThread().getName(), System.lineSeparator());
            log.info(msg);
            threadDump(); // 记录线程堆栈信息包括锁争用信息
        } catch (Exception ex) {
            log.error("RejectedPolicyWithReport rejectedExecution error", ex);
        }
        throw new RejectedExecutionException();
    }

    /**
     * 获取线程dump信息<p>
     * 注意: 该方法默认会记录所有线程和锁信息虽然方便debug, 使用时最好加开关和间隔调用, 否则可能会增加latency<p>
     * 1.当前线程的基本信息:id,name,state<p>
     * 2.堆栈信息<p>
     * 3.锁相关信息(可以设置不记录)<p>
     *  默认在log记录<p>
     * @return
     */
    private void threadDump() {
        long now = System.currentTimeMillis();
        // 每隔10分钟dump一次
        if (now - lastPrintTime < TEN_MINUTES_MILLS) {
            return;
        }
        if (!guard.tryAcquire()) {
            return;
        }
        // 异步dump线程池信息
        ExecutorService pool = Executors.newSingleThreadExecutor();
        pool.execute(() -> {
            try {
                ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
                StringBuilder sb = new StringBuilder();
                for (ThreadInfo threadInfo : threadMxBean.dumpAllThreads(true, true)) {
                    sb.append(getThreadDumpString(threadInfo));
                }
                log.error("thread dump info:", sb.toString());
            } catch (Exception e) {
                log.error("thread dump error", e);
            } finally {
                guard.release();
            }
            lastPrintTime = System.currentTimeMillis();
        });
        pool.shutdown();
    }

    @SuppressWarnings("all")
    private String getThreadDumpString(ThreadInfo threadInfo) {
        StringBuilder sb = new StringBuilder("\"" + threadInfo.getThreadName() + "\"" +
                " Id=" + threadInfo.getThreadId() + " " +
                threadInfo.getThreadState());
        if (threadInfo.getLockName() != null) {
            sb.append(" on " + threadInfo.getLockName());
        }
        if (threadInfo.getLockOwnerName() != null) {
            sb.append(" owned by \"" + threadInfo.getLockOwnerName() +
                    "\" Id=" + threadInfo.getLockOwnerId());
        }
        if (threadInfo.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (threadInfo.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;

        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
        for (; i < stackTrace.length && i < 32; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + threadInfo.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : lockedMonitors) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    public static RejectedPolicyWithReport newInstance() {
        return new RejectedPolicyWithReport();
    }
}
