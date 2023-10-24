package sa.qiwa.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import sa.qiwa.logging.util.LoggingUtil;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        LoggingUtil.logClientRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        LoggingUtil.logClientResponse(request, response);
        return response;
    }
}
