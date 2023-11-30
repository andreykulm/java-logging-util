package sa.qiwa.logging.util;

import com.google.common.io.CharStreams;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import sa.qiwa.logging.model.LoggingDataHolder;
import sa.qiwa.logging.model.LoggingRequestModel;
import sa.qiwa.logging.model.LoggingResponseModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class LoggingUtil {

    public static final String APP_LOGGER_NAME = "json_app";

    public static final Logger appLoger = LoggerFactory.getLogger(APP_LOGGER_NAME);
    public static final String BODY = "body";

    @SneakyThrows
    public static void logAppExchange(Instant started, CachedLogHttpServletRequest request, CachedLogHttpServletResponse response) {

        Instant end = Instant.now();
        LoggingRequestModel loggingRequestModel = LoggingRequestModel.builder()
                .path(requestUrl(request))
                .sessionKey("none")
                .method(request.getMethod())
                .headers(getHeaders(request))
                .message(Map.of(BODY, CharStreams.toString(request.getReader())))
                .build();

        LoggingResponseModel loggingResponseModel = LoggingResponseModel.builder()
                .headers(getHeaders(response))
                .success(HttpStatus.valueOf(response.getStatus()).is2xxSuccessful())
                .status(String.valueOf(response.getStatus()))
                .message(Map.of(BODY, response.getCached().getCopy()))
                .build();

        LoggingDataHolder.response.set(loggingResponseModel);
        LoggingDataHolder.protocol.set(request.getProtocol());
        LoggingDataHolder.request.set(loggingRequestModel);
        LoggingDataHolder.started.set(started);
        LoggingDataHolder.finished.set(end);
        LoggingDataHolder.elapsed.set(Duration.between(started, end));
        try {
            appLoger.info("App request response logging");
        } finally {
            LoggingDataHolder.clear();
        }
    }

    @SneakyThrows
    public static void logAppOutboundExchange(Instant started, HttpRequest request, byte[] body, ClientHttpResponse response) {

        Instant end = Instant.now();
        LoggingRequestModel loggingRequestModel = LoggingRequestModel.builder()
                .path(request.getURI().toString())
                .sessionKey("none")
                .method(request.getMethod().toString())
                .headers(request.getHeaders().toSingleValueMap())
                .message(Map.of(BODY, new String(body, StandardCharsets.UTF_8)))
                .build();

        LoggingResponseModel loggingResponseModel = LoggingResponseModel.builder()
                .headers(response.getHeaders().toSingleValueMap())
                .success(response.getStatusCode().is2xxSuccessful())
                .status(response.getStatusCode().toString())
                .message(Map.of(BODY, StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())))
                .build();

        LoggingDataHolder.response.set(loggingResponseModel);
        LoggingDataHolder.protocol.set(request.getURI().getScheme());
        LoggingDataHolder.request.set(loggingRequestModel);
        LoggingDataHolder.started.set(started);
        LoggingDataHolder.finished.set(end);
        LoggingDataHolder.elapsed.set(Duration.between(started, end));
        try {
            appLoger.info("Outbound request response logging");
        } finally {
            LoggingDataHolder.clear();
        }
    }

    @SneakyThrows
    public static void logIntegration(Instant started, HttpRequest request, byte[] body, ClientHttpResponse response) {

        Instant end = Instant.now();
        LoggingRequestModel loggingRequestModel = LoggingRequestModel.builder()
                .path(request.getURI().toString())
                .sessionKey("none")
                .method(request.getMethod().toString())
                .headers(request.getHeaders().toSingleValueMap())
                .message(Map.of(BODY, new String(body, StandardCharsets.UTF_8)))
                .build();

        LoggingResponseModel loggingResponseModel = LoggingResponseModel.builder()
                .headers(response.getHeaders().toSingleValueMap())
                .success(response.getStatusCode().is2xxSuccessful())
                .status(response.getStatusCode().toString())
                .message(Map.of(BODY, StreamUtils.copyToString(response.getBody(), Charset.defaultCharset())))
                .build();

        LoggingDataHolder.response.set(loggingResponseModel);
        LoggingDataHolder.protocol.set(request.getURI().getScheme());
        LoggingDataHolder.request.set(loggingRequestModel);
        LoggingDataHolder.started.set(started);
        LoggingDataHolder.finished.set(end);
        LoggingDataHolder.elapsed.set(Duration.between(started, end));
        try {
            appLoger.info("Outbound request response logging");
        } finally {
            LoggingDataHolder.clear();
        }
    }


    private static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                StringBuilder headerValueBuilder = new StringBuilder();
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    headerValueBuilder.append(headerValues.nextElement()).append(",");
                }
                headers.put(headerName, headerValueBuilder.toString());
            }
        }
        return headers;
    }

    private static Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new HashMap<>();
        Optional.ofNullable(response.getHeaderNames())
                .filter(it -> !it.isEmpty())
                .ifPresent(headerNames -> {
                    for (String headerName : headerNames) {
                        Optional.ofNullable(response.getHeaders(headerName))
                                .filter(it -> !it.isEmpty())
                                .map(headerValues -> String.join(",", headerValues))
                                .ifPresent(value -> {
                                    headers.put(headerName, value);
                                });
                    }
                });
        return headers;
    }


    private static String requestUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String query = request.getQueryString();
        if (StringUtils.hasText(query)) {
            url.append('?').append(query);
        }
        return url.toString();
    }
}
