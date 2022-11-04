package github.qiao712.bbs.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 利用Redis实现的简单的分布式锁
 */
public class DistributedLock {
    private final String lockName;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String identity = UUID.randomUUID().toString();
    private final Long lockValidTime;   //s

    //解锁脚本
    private final DefaultRedisScript<Long> unlockScript = new DefaultRedisScript<>(
            "local lockName = KEYS[1]\n" +
            "local identity = redis.call(\"GET\", lockName)\n" +
            "if identity == ARGV[1]\n" +
            "then return redis.call(\"DEL\", lockName)\n" +
            "else return 0\n" +
            "end", Long.class);

    //刷新时间，续期
    private final DefaultRedisScript<Long> refreshScript = new DefaultRedisScript<>(
            "local lockName = KEYS[1]\n" +
            "local identity = redis.call(\"GET\", lockName)\n" +
            "if identity == ARGV[1]\n" +
            "then return redis.call(\"EXPIRE\", lockName, ARGV[2])\n" +
            "else return 0 \n" +
            "end", Long.class);

    public DistributedLock(String lockName, long lockValidTime, RedisTemplate<String, Object> redisTemplate){
        this.lockName = "lock:" + lockName;
        this.lockValidTime = lockValidTime;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试加锁
     */
    public boolean tryLock(){
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockName, identity, lockValidTime, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 解锁
     */
    public void unlock(){
        Long result = redisTemplate.execute(unlockScript, Collections.singletonList(lockName), identity);
        if(!Objects.equals(result, 1L)){
            throw new RuntimeException("解锁失败");
        }
    }

    /**
     * 刷新过期时间，续期
     */
    public void refresh(){
        Long result = redisTemplate.execute(refreshScript, Collections.singletonList(lockName), identity, lockValidTime);
        if(!Objects.equals(result, 1L)){
            throw new RuntimeException("刷新失败");
        }
    }
}
