package ir.piana.boot.inquiry.common.httpclient;

import jakarta.annotation.PostConstruct;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Configuration
public class RestClientBeanCreatorConfig {
    private final GenericWebApplicationContext applicationContext;
    private final Clients clients;
    private final ResourceLoader resourceLoader;

    @ConfigurationProperties(prefix = "piana.tools.http-client")
    public record Clients(
            List<HttpClientProperties> clients
    ) {
    }

    @ConstructorBinding
    public RestClientBeanCreatorConfig(
            GenericWebApplicationContext applicationContext,
            Clients clients,
            ResourceLoader resourceLoader) {
        this.applicationContext = applicationContext;
        this.clients = clients;
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void onPostConstruct() {
        /*PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultTlsConfig(TlsConfig.custom()
                        .setHandshakeTimeout(Timeout.ofSeconds(30))
                        .setSupportedProtocols(TLS.V_1_2, TLS.V_1_3)
                        .build())
                .setDefaultSocketConfig(SocketConfig.custom()
                        .setSoTimeout(Timeout.ofMinutes(1))
                        .build())
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
                .setConnPoolPolicy(PoolReusePolicy.LIFO)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setSocketTimeout(Timeout.ofMinutes(1))
                        .setConnectTimeout(Timeout.ofMinutes(1))
                        .setTimeToLive(TimeValue.ofMinutes(10))
                        .build())
                .build();*/

        clients.clients.forEach(client -> {
            try {
                PoolingHttpClientConnectionManagerBuilder builder = PoolingHttpClientConnectionManagerBuilder.create();
                if (client.isSecure()) {
                    List<TLS> tlsVersions = null;
                    List<String> tlsVersionStrings = client.tlsVersions();
                    if (tlsVersionStrings != null && !tlsVersionStrings.isEmpty()) {
                        tlsVersions = tlsVersionStrings.stream().map(TLS::valueOf).toList();
                    } else {
                        tlsVersions = List.of(TLS.V_1_3);
                    }
                    if (client.trustStore() == null || client.trustStore().isEmpty()) {
                        builder.setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(SSLContexts.custom()
                                        .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                                        .build())
                                .setTlsVersions(tlsVersions.toArray(new TLS[0]))
                            .build());
                    } else {
                        KeyStore ks = KeyStore.getInstance("JKS");
                        if (client.trustStore().startsWith("classpath:")) {
                            Resource resource = resourceLoader.getResource(client.trustStore());
                            ks.load(resource.getInputStream(), client.trustStorePassword().toCharArray());
                        } else {
                            File trustFile = new File(client.trustStore());
                            ks.load(new FileInputStream(trustFile), client.trustStorePassword().toCharArray());
                        }

                        builder.setSSLSocketFactory(SSLConnectionSocketFactoryBuilder.create()
                                .setSslContext(SSLContexts.custom()
                                        .loadTrustMaterial(
                                                ks,
                                                (X509Certificate[] chain, String authType) -> true)
                                        .build())
                                .setTlsVersions(tlsVersions.toArray(new TLS[0]))
                                .build());
                        //ToDo load from trustStore
                    }
                }

                builder.setDefaultSocketConfig(SocketConfig.custom()
                                .setSoTimeout(Timeout.ofSeconds(
                                        Optional.ofNullable(client.soTimeout()).orElse(20L)))
                                .build())
                        .setPoolConcurrencyPolicy(Optional.ofNullable(client.poolConcurrencyPolicy())
                                .map(PoolConcurrencyPolicy::valueOf).orElse(PoolConcurrencyPolicy.STRICT))
                        .setConnPoolPolicy(Optional.ofNullable(client.poolReusePolicy())
                                .map(PoolReusePolicy::valueOf).orElse(PoolReusePolicy.LIFO))
                        .setDefaultConnectionConfig(ConnectionConfig.custom()
                                .setSocketTimeout(Timeout.ofSeconds(
                                        Optional.ofNullable(client.socketTimeout()).orElse(30L)))
                                .setConnectTimeout(Timeout.ofSeconds(
                                        Optional.ofNullable(client.connectionTimeout()).orElse(30L)))
                                .setTimeToLive(Timeout.ofSeconds(
                                        Optional.ofNullable(client.timeToLive()).orElse(600L)))
                                .build())
                        .build();
                Logger log = LoggerFactory.getLogger(client.name());
                final CloseableHttpClient httpClient = HttpClientBuilder
                        .create()
                        .setConnectionManager(builder.build())
                        .build();

                HttpComponentsClientHttpRequestFactory requestFactory =
                        new HttpComponentsClientHttpRequestFactory();

                requestFactory.setHttpClient(httpClient);

                RestClient restClient = RestClient.builder()
                        .requestFactory(requestFactory)
                        .baseUrl((client.isSecure() ? "https://" : "http://") +
                                client.host() + ":" + client.port() + "/" +
                                (client.baseUrl() != null ? client.baseUrl() + "/" : ""))
                        .requestInterceptor((request, body, execution) -> {
                            logRequest(log, request, body);
                            var response = new BufferingClientHttpResponseWrapper(execution.execute(request, body));
                            logResponse(log, request, response);
                            response.reset();
                            return response;
                        })
                        .build();
                applicationContext.registerBean(client.name(), RestClient.class, () -> restClient);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void logRequest(Logger log, HttpRequest request, byte[] body) throws IOException {
        log.info("Request: {} {}", request.getMethod(), request.getURI());
        logHeaders(log, request.getHeaders());
        if (body != null && body.length > 0) {
            log.info("Request body: {}", new String(body, StandardCharsets.UTF_8));
        }
    }

    private void logResponse(Logger log, HttpRequest request, ClientHttpResponse response) throws IOException {
        log.info("Response status: {}", response.getStatusCode().value());
        logHeaders(log, response.getHeaders());
        byte[] responseBody = response.getBody().readAllBytes();
        if (responseBody.length > 0) {
            log.info("Response body: {}", new String(responseBody, StandardCharsets.UTF_8));
        }
    }

    private void logHeaders(Logger log, HttpHeaders headers) throws IOException {
        log.info("Headers: {}", headers.entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(";")));
    }
}
