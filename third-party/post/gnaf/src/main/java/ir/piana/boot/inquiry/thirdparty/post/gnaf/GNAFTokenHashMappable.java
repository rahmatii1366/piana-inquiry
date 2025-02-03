package ir.piana.boot.inquiry.thirdparty.post.gnaf;

import ir.piana.dev.utils.jedisutils.JedisPool;
import ir.piana.dev.utils.jedisutils.RedisHashMappable;

import java.util.*;

public class GNAFTokenHashMappable implements RedisHashMappable {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;

    public GNAFTokenHashMappable() {
    }

    public GNAFTokenHashMappable(String accessToken, String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(JedisPool jedisPool, String accessToken) {
        this.accessToken = accessToken;
        jedisPool.flushField(this, "accessToken");
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(JedisPool jedisPool, String tokenType) {
        this.tokenType = tokenType;
        jedisPool.flushField(this, "tokenType");
    }

    @Override
    public Map<String, String> toRedisHashMap() {
        return new LinkedHashMap<>() {{
            put("accessToken", accessToken);
            put("tokenType", tokenType);
        }};
    }

    public static GNAFTokenHashMappable fromRedisHashMap(Map<String, String> hashMap) {
        return new GNAFTokenHashMappable(
                hashMap.get("accessToken"),
                hashMap.get("tokenType"),
                0l
        );
    }

    @Override
    public void reloadFromRedisHashMap(Map<String, String> map) {
        GNAFTokenHashMappable GNAFTokenHashMappable = fromRedisHashMap(map);
        this.accessToken = GNAFTokenHashMappable.accessToken;
        this.tokenType = GNAFTokenHashMappable.tokenType;
    }

    @Override
    public List<String> redisKeys() {
        return List.of("accessToken", "tokenType");
    }

    @Override
    public String redisHashKey(String... strings) {
        return "gnaf-token";
    }

    @Override
    public Long expireIn() {
        return expiresIn;
    }
}
