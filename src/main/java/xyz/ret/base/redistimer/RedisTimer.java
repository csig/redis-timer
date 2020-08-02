package xyz.ret.base.redistimer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import xyz.ret.base.common.JsonUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisTimer extends Timer implements Runnable {
    private String redisKey = "redis-timer";
    private int fetchNum = 1;

    private final RedisQueue redisQueue;
    private volatile boolean run = true;
    private Thread thread;

    public RedisTimer(RedisTemplate<String, Object> redisTemplate) {
        super();
        redisQueue = new RedisQueue(redisTemplate);
    }

    public void setRedisKey(String key) {
        this.redisKey = key;
    }

    public void setFetchNum(int num) {
        this.fetchNum = num;
    }

    public synchronized void begin() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        } else {
            log.warn("The timer has been running.");
        }
    }

    public void stop() {
        run = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        log.info("Redis timer has started.");
        while (run){
            ArrayList<Task> tasks = new ArrayList<>();

            long score = System.currentTimeMillis();
            List<Task> listInTime = redisQueue.pop(redisKey, (double)score, fetchNum);
            if(!CollectionUtils.isEmpty(listInTime)){
                tasks.addAll(listInTime);
            }

            /* if queue is empty, delay one second */
            if (CollectionUtils.isEmpty(tasks)) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ignored) {
                }
            } else {
                StopWatch sw = StopWatch.createStarted();
                tasks.forEach(t -> {
                    try {
                        handle(t);
                    } catch (Exception e) {
                        log.error("Task execute failed [type]{}[obj]{}", t.getType(), JsonUtils.toJson(t.getParam()));
                    }
                });
                sw.stop();
                log.info("Fetch task num {}, time consume {} ms.", tasks.size(), sw.getTime());
            }
        }
        log.info("Redis timer has stopped.");
    }

    @Override
    public void execute(String type, Object param) {
        redisQueue.push(redisKey, (double) System.currentTimeMillis(), new Task(type, param));
    }

    @Override
    public void schedule(String type, Object param, Date date) {
        redisQueue.push(redisKey, (double)date.getTime(), new Task(type, param));
    }

    @Override
    public void schedule(String type, Object param, Long timestamp) {
        redisQueue.push(redisKey, (double)timestamp, new Task(type, param));
    }

    @Override
    public void schedule(String type, Object param, long delay, TimeUnit unit) {
        long timeForMilliSeconds = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(delay, unit);
        redisQueue.push(redisKey, (double)timeForMilliSeconds, new Task(type, param));
    }
}
