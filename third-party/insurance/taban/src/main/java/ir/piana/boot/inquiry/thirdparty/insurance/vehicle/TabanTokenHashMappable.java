package ir.piana.boot.inquiry.thirdparty.insurance.vehicle;

import ir.piana.dev.utils.jedisutils.JedisPool;
import ir.piana.dev.utils.jedisutils.RedisHashMappable;

import java.util.*;

public class TabanTokenHashMappable implements RedisHashMappable {
    private String accessToken;
    private String scope;
    private String tokenType;
    private Long expiresIn;

    public TabanTokenHashMappable() {
    }

    public TabanTokenHashMappable(String accessToken, String scope, String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.scope = scope;
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

    public String getScope() {
        return scope;
    }

    public void setScope(JedisPool jedisPool, String scope) {
        this.scope = scope;
        jedisPool.flushField(this, "scope");
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
            put("scope", scope);
            put("tokenType", tokenType);
        }};
    }

    public static TabanTokenHashMappable fromRedisHashMap(Map<String, String> hashMap) {
        return new TabanTokenHashMappable(
                hashMap.get("accessToken"),
                hashMap.get("scope"),
                hashMap.get("tokenType"),
                0l
        );
    }

    @Override
    public void reloadFromRedisHashMap(Map<String, String> map) {
        TabanTokenHashMappable tabanTokenHashMappable = fromRedisHashMap(map);
        this.accessToken = tabanTokenHashMappable.accessToken;
        this.scope = tabanTokenHashMappable.scope;
        this.tokenType = tabanTokenHashMappable.tokenType;
    }

    @Override
    public List<String> redisKeys() {
        return List.of("accessToken", "scope", "tokenType");
    }

    @Override
    public String redisHashKey(String... strings) {
        return "taban-token";
    }

    @Override
    public Long expireIn() {
        return expiresIn;
    }
}
