package sa.qiwa.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import sa.qiwa.logging.util.LoggingUtil;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class IbmLoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);
        Instant started = Instant.now();

        LoggingUtil.logAppOutboundExchange(started, request, body, response);
        return response;
    }
}
