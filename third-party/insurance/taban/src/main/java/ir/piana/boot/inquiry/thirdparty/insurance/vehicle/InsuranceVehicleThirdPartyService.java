package ir.piana.boot.inquiry.thirdparty.insurance.vehicle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.piana.boot.utils.errorprocessor.internal.InternalServerError;
import ir.piana.dev.utils.jedisutils.JedisPool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InsuranceVehicleThirdPartyService {
    private final ObjectMapper objectMapper;
    private final JedisPool jedisPool;
    private final RestClient restClient;

    @Value("${piana.config.third-parties.taban.token}")
    private String token;

    public InsuranceVehicleThirdPartyService(
            @Qualifier("taban") RestClient restClient,
            JedisPool jedisPool,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    private TabanTokenHashMappable getAuthToken() {
        try {
            TabanTokenHashMappable redisHashMappable = jedisPool.getRedisHashMappable(TabanTokenHashMappable.class);
            if (redisHashMappable == null) {
                redisHashMappable = restClient.post()
                        .uri("token?grant_type=client_credentials")
//                    .header("Postman-Token", "7c15e34c-00e2-4303-b69a-35308b3e2bae")
//                    .header("User-Agent", "PostmanRuntime/7.37.3")
                        .header(
                                "Authorization",
                                "Basic " + token)
                        .exchange((clientRequest, clientResponse) -> {
                            if (clientResponse.getStatusCode().is2xxSuccessful()) {
                                JsonNode jsonNode = objectMapper.readTree(clientResponse.getBody());
                                TabanTokenHashMappable tabanTokenHashMappable = new TabanTokenHashMappable(
                                        jsonNode.get("access_token").asText(),
                                        jsonNode.get("scope").asText(),
                                        jsonNode.get("token_type").asText(),
                                        jsonNode.get("expires_in").asLong()
                                );
                                jedisPool.setRedisHashMappable(tabanTokenHashMappable);
                                return tabanTokenHashMappable;
                            }
                            return null;
                        });

                return redisHashMappable;
            }
            return redisHashMappable;
        } catch (Exception e) {
            throw InternalServerError.exception;
        }
    }

    public JsonNode getByVin(String vin, String productionYear) {
        try {
            TabanTokenHashMappable authToken = getAuthToken();
            return restClient.post()
                    .uri("VSBV/1/api/ThirdPartyInsurance/SearchByVin")
                    .header("token", "Bearer " + authToken.getAccessToken())
                    .header(
                            "Authorization",
                            "Basic " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.createObjectNode()
                            .put("vin", vin)
                            .put("productionYear", productionYear))
                    .exchange((clientRequest, clientResponse) ->
                            objectMapper.readTree(clientResponse.getBody().readAllBytes()));
        } catch (Exception e) {
            throw InternalServerError.exception;
        }
    }
}
