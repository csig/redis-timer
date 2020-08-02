package xyz.ret.base.redistimer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.CollectionUtils;
import xyz.ret.base.common.JsonUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedisQueue {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisQueue(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void push(String key, Double score, Task value){
        if (score <= 0) {
            throw new RuntimeException("Score should bigger then zero.");
        }

        String jsonString = JsonUtils.toJsonWithDefaultType(value);
        redisTemplate.opsForZSet().add(key, jsonString, score);
    }

    public List<Task> pop(String key, Double maxScore, Integer maxCount){
        String script =
                "local result = redis.call('zrangebyscore', KEYS[1], ARGV[1], ARGV[2], 'LIMIT', 0, ARGV[3]) " +
                "if #result > 0 then " +
                "   redis.call('zremrangebyrank', KEYS[1], '0', #result - 1) " +
                "end " +
                "return result ";

        List<String> keys = new ArrayList<>();
        keys.add(key);

        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>(script, List.class);
        List<String> taskStrList = redisTemplate.execute(redisScript, keys, 0, maxScore, maxCount);

        List<Task> retList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(taskStrList)) {
            for(String taskStr : taskStrList){
                Task t = (Task)JsonUtils.toBeanWithDefaultType(taskStr);
                if(t == null){
                    log.error("Can not retrieve task from string: {}", taskStr);
                }else{
                    retList.add(t);
                }
            }
        }
        return retList;
    }

}
