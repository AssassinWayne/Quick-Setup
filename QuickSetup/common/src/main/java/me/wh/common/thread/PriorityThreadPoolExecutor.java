package me.wh.common.thread;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityThreadPoolExecutor extends ThreadPoolExecutor {

    public static final int mCorePoolSize = 8;
    private static final int mMaximumPoolSize = Integer.MAX_VALUE;
    private static final long mKeepAliveTime = 10000;
    private static final TimeUnit mTimeUnit = TimeUnit.MILLISECONDS;
    private final AtomicInteger mPriorityAtomic = new AtomicInteger(1);

    private static final class PriorityRunnable implements Runnable {
        private final Runnable runnable;
        private final int priority;

        PriorityRunnable(Runnable runnable, int priority) {
            this.runnable = runnable;
            this.priority = priority;
        }

        PriorityRunnable(Runnable runnable) {
            this.runnable = runnable;
            this.priority = 0;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public void run() {
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public PriorityThreadPoolExecutor() {
        super(mCorePoolSize, mMaximumPoolSize, mKeepAliveTime, mTimeUnit,
                new PriorityBlockingQueue<Runnable>(mCorePoolSize, new Comparator<Runnable>() {
                    @Override
                    public int compare(Runnable lhs, Runnable rhs) {
                        if (lhs instanceof PriorityRunnable && rhs instanceof PriorityRunnable) {
                            return ((PriorityRunnable) lhs).priority - ((PriorityRunnable) rhs).priority;
                        } else if (lhs instanceof PriorityRunnable) {
                            return 1;
                        } else if (rhs instanceof PriorityRunnable) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }),
                new RunOldRejectedExecutionHandler());
    }

    @Override
    public void execute(Runnable command) {
        super.execute(new PriorityRunnable(command, mPriorityAtomic.getAndIncrement()));
    }

    @Override
    public boolean remove(Runnable task) {
        BlockingQueue<Runnable> queueOld = super.getQueue();
        boolean remove = false;
        if (queueOld != null && !queueOld.isEmpty()) {
            BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>(queueOld);
            for (Runnable r : queue) {
                if (r == task) {
                    remove = super.remove(r);
                } else if (r instanceof PriorityRunnable) {
                    if (((PriorityRunnable) r).getRunnable() == task) {
                        remove = super.remove(r);
                    }
                }
            }
        }
        return remove;
    }
}
