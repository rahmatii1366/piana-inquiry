package ir.piana.boot.inquiry.thirdparty.post.gnaf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.piana.boot.utils.errorprocessor.ApiException;
import ir.piana.boot.utils.errorprocessor.internal.InternalServerError;
import ir.piana.dev.utils.jedisutils.JedisPool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class GNAFService {
    private final ObjectMapper objectMapper;
    private final JedisPool jedisPool;
    private final RestClient restClient;

    @Value("${piana.config.third-parties.gnaf.username}")
    private String username;
    @Value("${piana.config.third-parties.gnaf.password}")
    private String password;

    public GNAFService(
            @Qualifier("gnaf") RestClient restClient,
            JedisPool jedisPool,
            ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.jedisPool = jedisPool;
        this.objectMapper = objectMapper;
    }

    private GNAFTokenHashMappable getAuthToken() {
        try {
            GNAFTokenHashMappable redisHashMappable = jedisPool.getRedisHashMappable(GNAFTokenHashMappable.class);
            if (redisHashMappable == null) {
                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("grant_type", "password");
                map.add("username", username);
                map.add("password", password);

                redisHashMappable = restClient.post()
                        .uri("token?grant_type=client_credentials")
                        .header("content-type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .body(map)
                        .exchange((clientRequest, clientResponse) -> {
                            if (clientResponse.getStatusCode().is2xxSuccessful()) {
                                JsonNode jsonNode = objectMapper.readTree(clientResponse.getBody());
                                GNAFTokenHashMappable GNAFTokenHashMappable = new GNAFTokenHashMappable(
                                        jsonNode.get("access_token").asText(),
                                        jsonNode.get("token_type").asText(),
                                        jsonNode.get("expires_in").asLong()
                                );
                                jedisPool.setRedisHashMappable(GNAFTokenHashMappable);
                                return GNAFTokenHashMappable;
                            }
                            throw InternalServerError.exception;
                        });
                return redisHashMappable;
            }
            return redisHashMappable;
        } catch (Exception e) {
            throw InternalServerError.exception;
        }
    }

    public static final String COULD_NOT_GET_POSTAL_INFORMATION = "could not get address with provider";

    public JsonNode postalCodeInfo(String postalCode) {
        try {
            GNAFTokenHashMappable authToken = getAuthToken();
            return restClient.post()
                    .uri("api/v0/Postcode/AddressByPostcode")
                    .header(
                            "Authorization",
                            "Bearer " + authToken.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.createObjectNode()
                            .put("ClientBatchID", now())
                            .put("Signature", "")
                            .put("PostCodes", objectMapper.createArrayNode()
                                    .add(objectMapper.createObjectNode()
                                            .put("ClientRowID", now())
                                            .put("PostCode", postalCode))))
                    .exchange((clientRequest, clientResponse) -> {
                        if (clientResponse.getStatusCode().is2xxSuccessful()) {
                            return objectMapper.readTree(clientResponse.getBody().readAllBytes());
                        }
                        else {
                            throw ApiException.customApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    COULD_NOT_GET_POSTAL_INFORMATION);
                        }
                    });

        } catch (Exception e) {
            throw InternalServerError.exception;
        }
    }

    /**
     * Returns current time in format specified by GNAF.
     *
     * @return current time in long value
     */
    private long now() {
        return Long.parseLong(FORMAT_SPECIFIED_BY_GNAF.format(Instant.now().atZone(ASIA_TEHRAN_ZONE_ID)));
    }

    public static final DateTimeFormatter FORMAT_SPECIFIED_BY_GNAF = DateTimeFormatter.ofPattern("yyMMddHHmmssSSSS");
    public static final ZoneId ASIA_TEHRAN_ZONE_ID = ZoneId.of("Asia/Tehran");

}
