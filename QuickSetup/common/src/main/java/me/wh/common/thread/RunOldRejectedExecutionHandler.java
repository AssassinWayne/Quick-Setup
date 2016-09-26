package me.wh.common.thread;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RunOldRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!executor.isShutdown()) {
            Runnable runnable = executor.getQueue().poll();
            executor.execute(r);
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}
