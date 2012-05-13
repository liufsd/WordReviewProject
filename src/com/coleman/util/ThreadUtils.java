package com.coleman.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 管理线程池的工具类 亲，当你需要执行一个异步操作(比较耗时),希望能个你带来方便 你可以调用{@link #execute(Runnable)}来帮你执行
 * 
 * @author andyzhang
 * */
public class ThreadUtils
{
    /** 创建线程池 */
    private static ExecutorService executor = null;
    
    /**
     * 在{@link RCSApplication#onCreate}调用这个方法去初始化线程池
     * */
    public synchronized static void prepare()
    {
        if (executor == null || executor.isShutdown())
        {
            executor = null;
            executor = Executors.newCachedThreadPool();
        }
    }
    
    /**
     * 在{@link RCSApplication#onTerminate}的时候销毁线程池
     * */
    public synchronized static void shutdown()
    {
        if (executor != null)
        {
            if (!executor.isShutdown())
            {
                executor.shutdown();
            }
            executor = null;
        }
    }
    
    /**
     * 执行Runnable形式的任务
     * 
     * @param task
     *            需要执行的任务
     * */
    public static void execute(Runnable task)
    {
        executor.execute(task);
    }
    
    /**
     * 执行Callable形式的任务
     * 
     * @param task
     *            需要执行的任务
     * @return 任务执行结束返回结果信息
     * */
    public static Future<?> submit(Callable<?> task)
    {
        return executor.submit(task);
    }
    
}
