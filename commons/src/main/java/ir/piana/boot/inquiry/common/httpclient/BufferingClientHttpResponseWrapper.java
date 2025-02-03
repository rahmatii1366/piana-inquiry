package ir.piana.boot.inquiry.common.httpclient;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferingClientHttpResponseWrapper implements ClientHttpResponse {
    private final ClientHttpResponse response;
//    private final byte[] body;

    private ByteArrayInputStream bis;

    public BufferingClientHttpResponseWrapper(
            ClientHttpResponse response/*, byte[] body*/) {
        this.response = response;
        try {
            if (bis == null) {
                bis = new ByteArrayInputStream(
                        response.getBody().readAllBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        this.body = body;
    }

    @Override
    public InputStream getBody() {
        return bis;
    }

    void reset() throws IOException {
        bis.reset();
    }

    // Delegate other methods to wrapped response
    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }
}
