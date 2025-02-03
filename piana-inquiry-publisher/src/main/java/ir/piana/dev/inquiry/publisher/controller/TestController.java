package ir.piana.dev.inquiry.publisher.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("api/v1/inquiry/publisher/test")
public class TestController {
    private final RestClient restClient;

    public TestController(@Qualifier("self") RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping
    public ResponseEntity<String> sayHello() {

        RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse exchange = restClient.get()
                .uri("api/v1/inquiry/insurance/vehicle/third-party/search-by-vin")
                .header("auth-type", "basic")
                .header(
                        "Authorization",
                        "Basic bWoucmFobWF0aToxMjM0NTY=")
                .exchange((clientRequest, clientResponse) -> {
                    return clientResponse;
                });

        return ResponseEntity.ok("hello!");
    }
}
