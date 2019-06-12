package com.devhc.quicklearning.scheduler;

public abstract class BaseScheduler {
    public abstract void start() throws Exception;
    public abstract void stop() throws Exception;

    public abstract void init() throws Exception;
}
