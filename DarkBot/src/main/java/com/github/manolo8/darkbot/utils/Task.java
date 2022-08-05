package com.github.manolo8.darkbot.utils;

public class Task {
    private final Runnable task;
    private long runAt = -1;
    private long limitedDelay = -1;

    public Task(Runnable task) {
        if (task == null) throw new IllegalArgumentException("Task shouldn't be null");
        this.task = task;
    }

    public void cancel(){
        runAt = -1;
    }

    public void schedule(long delay){
        limitedDelay = -1;
        if (runAt == -1) this.runAt = System.currentTimeMillis() + delay;
        else if (System.currentTimeMillis() > runAt) forceExecute();
    }

    public void tick(){
        if (runAt != -1 && System.currentTimeMillis() > runAt) forceExecute();
    }

    public void limit(long delay) {
        limitedDelay = delay;
        if (System.currentTimeMillis() < runAt) return;
        task.run();
        runAt = System.currentTimeMillis() + delay;
    }

    public void forceExecute() {
        if(limitedDelay == -1) cancel();
        else runAt = System.currentTimeMillis() + limitedDelay;
        task.run();
    }
}
