package com.future.concurrent.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class DefaultThreadPool implements ThreadPool {

    //最大核心线程数：Runtime.getRuntime().availableProcessors() * 2
    private static final Integer MAX_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    //最大线程数
    private static final Integer MAX_NUM_POOL_SIZE = MAX_CORE_POOL_SIZE *  4;
    //默认队列大小
    private static final Integer DEFAULT_QUEUE_SIZE = 1024;
    //默认线程存活时间
    private static final Long DEFAULT_ALIVE_TIME = 60L;

    // 核心线程数
    private final Integer corePoolSize ;
    // 最大线程数
    private final Integer maxmumPoolSize;
    // 线程池名称格式
    private final String threadPoolName;
    // 线程工厂名称
    private final ThreadFactory threadFactory;
    // 队列大小
    private final Integer queueDefaultSize;
    // 线程存活时间
    private final Long defaultAliveTime;
    // 决绝策略
    private final RejectedExecutionHandler rejectedExecutionHandler;

    private final ExecutorService executor;

    private DefaultThreadPool(Builder builder) {
        this.corePoolSize = (builder.corePoolSize > MAX_CORE_POOL_SIZE ? MAX_CORE_POOL_SIZE : builder.corePoolSize);
        this.maxmumPoolSize = ((null == builder.maxmumPoolSize || builder.maxmumPoolSize <= 0 || builder.maxmumPoolSize < this.corePoolSize) ? MAX_NUM_POOL_SIZE : builder.maxmumPoolSize);
        this.threadPoolName = null == builder.threadPoolName || "".equals(builder.threadPoolName) ? "ExternalConvertProcessPool-%d" : builder.threadPoolName;
        this.threadFactory = (null == builder.threadFactory ? Executors.defaultThreadFactory() : builder.threadFactory);
        this.queueDefaultSize = ((null == builder.queueDefaultSize || builder.queueDefaultSize <= 0) ? DEFAULT_QUEUE_SIZE : builder.queueDefaultSize);
        this.defaultAliveTime = ((null == builder.defaultAliveTime || builder.defaultAliveTime < 0) ? DEFAULT_ALIVE_TIME : builder.defaultAliveTime);
        this.rejectedExecutionHandler = (null == builder.rejectedExecutionHandler ? RejectedPolicyWithReport.newInstance() : builder.rejectedExecutionHandler);
        final BlockingQueue<Runnable> executeQueue = new ArrayBlockingQueue<>(this.queueDefaultSize);
        this.executor = new ThreadPoolExecutor(this.corePoolSize, this.maxmumPoolSize,
                this.defaultAliveTime, TimeUnit.SECONDS, executeQueue, this.threadFactory, this.rejectedExecutionHandler);

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    // 日志
                    log.info("shutdown thread pool gracefully");
                    if (!this.executor.isShutdown()) {
                        executor.shutdown();
                        try {
                            if (!executor.awaitTermination(6, TimeUnit.SECONDS)) {
                                executor.shutdownNow();
                            }
                        } catch (InterruptedException e) {
                            // log
                            log.info("faile to shutdown thread pool gracefully");
                            executor.shutdownNow();
                        }
                    }
                }
        ));
    }

    public static Builder builder(final Integer corePoolSize) {
        return new Builder(corePoolSize);
    }

    public static class Builder {
        // 核心线程数-默认：Runtime.getRuntime().availableProcessors() * 2
        private final Integer corePoolSize ;
        // 最大线程数
        private Integer maxmumPoolSize;
        // 线程池名称格式
        private String threadPoolName;
        // 线程工厂名称
        private ThreadFactory threadFactory;
        // 队列大小
        private Integer queueDefaultSize;
        // 线程存活时间
        private Long defaultAliveTime;
        // 决绝策略
        private RejectedExecutionHandler rejectedExecutionHandler;

        public Builder(Integer corePoolSize) {
            this.corePoolSize = corePoolSize;
        }
        public Builder maxmumPoolSize(final Integer maxmumPoolSize) {
            this.maxmumPoolSize = maxmumPoolSize;
            return this;
        }

        public Builder threadPoolName(final String threadPoolName) {
            this.threadPoolName = threadPoolName;
            return this;
        }

        public Builder threadFactory(final ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public Builder queueDefaultSize(final Integer queueDefaultSize) {
            this.queueDefaultSize = queueDefaultSize;
            return this;
        }

        public Builder defaultAliveTime(final Long defaultAliveTime) {
            this.defaultAliveTime = defaultAliveTime;
            return this;
        }

        public Builder rejectedExecutionHandler(final RejectedExecutionHandler rejectedExecutionHandler) {
            this.rejectedExecutionHandler = rejectedExecutionHandler;
            return this;
        }

        public DefaultThreadPool build() {
            return new DefaultThreadPool(this);
        }

    }


    @Override
    public void execute(Runnable task) {
        try {
            this.executor.execute(new RunnableWrapper(task));
        } catch (Exception e) {
            log.error("execute task failed: {}", e.getMessage());
        }
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            return this.executor.submit(new CallableWrapper(task));
        } catch (Exception e) {
            log.error("unable to submit Callable task rejected");
        }
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        try {
            return this.executor.submit(new RunnableWrapper(task), result);
        } catch (Exception e) {
            log.error("unable to submit Runnable task with result : {}, rejected", result);
        }
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        try {
            return this.executor.submit(new RunnableWrapper(task));
        } catch (Exception e) {
            log.error("unable to submit Runnable task, rejected");
        }

        return null;
    }

    /**
     * Runnable task 包装类，记录异常日志，避免executor “吃掉” ex
     */
    public static class RunnableWrapper implements Runnable{
        private final Runnable task;
        public RunnableWrapper(Runnable targetTask) {
            this.task = targetTask;
        }

        @Override
        public void run() {
            if (null != task) {
                try {
                    task.run();
                } catch (Exception e) {
                    // 记录日志 wraped targetTask exception, e
                    log.error("execute target task failed: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Callable task 包装类，记录异常日志，避免executor “吃掉” ex
     */
    public static class CallableWrapper implements Callable{
        private final Callable task;

        public CallableWrapper(Callable targetTask) {
            this.task = targetTask;
        }

        @Override
        public Object call() throws Exception {
            try {
                return task.call();
            } catch (Exception e) {
                log.error("call target task failed: {}", e.getMessage());
            }
            return null;
        }
    }

}
