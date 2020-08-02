# redis-timer
A distributed timer based on redis sorted set.

# Usage

## Basic usage

> Initial a RedisTimer instance with a RedisTemplate instance. If you use jackson as serializer/deserializer, you should enable default typing. So that you can get the correct object type in function Action.handle(Object obj), but not a LinkedHashMap.

```java
RedisTimer timer = new RedisTimer(timerRedisTemplate);
timer.addAction("test", param -> {
    log.info("time arrive {}", param);
});
timer.begin();
```
> Use timer
```java
Date executeTime = new Date(); //some time
redisTimer.schedule("test", param, executeTime);
```


## Usage in spring boot
```java
@Configuration
@Slf4j
public class RedisTimerConfiguration {
    @Bean(name = "timerRedisTemplate")
    public RedisTemplate<String, Object> timerRedisTemplate(RedisConnectionFactory factory) {
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();

        return template;
    }


    @Bean(name = "redisTimer")
    public Timer redisTimer(RedisTemplate<String, Object> timerRedisTemplate) {
        RedisTimer timer = new SpringRedisTimer(timerRedisTemplate);
        timer.addAction("test", p -> {
            log.info("time arrive {}", p);
        });
        timer.setFetchNum(2);
        timer.setRedisKey("redis-timer");
        return timer;
    }

    static class SpringRedisTimer extends RedisTimer implements DisposableBean, CommandLineRunner {

        public SpringRedisTimer(RedisTemplate<String, Object> redisTemplate) {
            super(redisTemplate);
        }

        @Override
        public void destroy() {
            log.info("Stopping redis timer.");
            stop();
        }

        @Override
        public void run(String... args) {
            log.info("Starting redis timer.");
            begin();
        }
    }
}
```
