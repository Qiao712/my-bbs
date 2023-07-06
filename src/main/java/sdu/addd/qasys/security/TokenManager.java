package sdu.addd.qasys.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import sdu.addd.qasys.dto.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 维护Token和在线用户信息
 */
@Component
public class TokenManager {
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 储存登录信息，生成Token
     */
    public String createToken(AuthUser authUser, Long validTime){
        removeToken(authUser.getId());
        String token = UUID.randomUUID().toString();

        //token -- AuthUser
        String authUserJson = JSON.toJSONString(authUser);
        redisTemplate.opsForValue().set("Token:" + token, authUserJson, validTime, TimeUnit.SECONDS);
        //user id -- token
        redisTemplate.opsForValue().set("UserId:" + authUser.getId(), token, validTime, TimeUnit.SECONDS);

        return token;
    }

    /**
     * 清除令牌和用户信息
     */
    public void removeToken(Long userId){
        String userIdKey = "UserId:" + userId;
        String token = redisTemplate.opsForValue().get(userIdKey);
        if(token != null){
            redisTemplate.delete(userIdKey);
            redisTemplate.delete("Token:" + token);
        }
    }

    /**
     * 获取Token所对应的用户的信息
     */
    public AuthUser getUser(String token){
        String json = redisTemplate.opsForValue().get("Token:" + token);
        try{
            return JSON.parseObject(json, AuthUser.class);
        }catch (JSONException e){
            return null;
        }
    }
}
