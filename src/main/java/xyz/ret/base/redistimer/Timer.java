package xyz.ret.base.redistimer;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class Timer {
    private Map<String, Action> actions;

    public Timer() {
        actions = new HashMap<>();
    }

    public void addAction(String type, Action action) {
        actions.put(type, action);
    }

    public void handle(Task task) {
        Action action = actions.get(task.getType());
        if (action == null) {
            log.warn("Can not find action for type {}", task.getType());
        } else {
            action.handle(task.getParam());
        }
    }

    abstract public void execute(String type, Object param);
    abstract public void schedule(String type, Object param, Date date);
    abstract public void schedule(String type, Object param, Long timestamp);
    abstract public void schedule(String type, Object param, long delay, TimeUnit unit);
}
