package ir.piana.boot.inquiry.common.httpclient;

import java.util.List;

public record HttpClientProperties(
        String name,
        boolean isSecure,
        String host,
        String port,
        String baseUrl,
        Long soTimeout,
        Long connectionTimeout,
        Long socketTimeout,
        Long timeToLive,
        String poolReusePolicy, // LIFO, FIFO
        String poolConcurrencyPolicy,
        String trustStore,
        String trustStorePassword,
        List<String> tlsVersions
        ) {
}
