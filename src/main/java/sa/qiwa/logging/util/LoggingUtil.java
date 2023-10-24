package sa.qiwa.logging.util;

import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class LoggingUtil {


    public static void logServerRequest(CachedLogHttpServletRequest request) throws IOException {
        log.info("Server request: "
                + "\nURI               : " + request.getRequestURI()
                + "\nMethod            : " + request.getMethod()
                + "\nRequest parameters: " + request.getQueryString()
                + "\nHeaders           : " + getHeaders(request)
                + "\nRequest body      : " + CharStreams.toString(request.getReader()));
    }


    public static void logServerResponse(CachedLogHttpServletRequest request, HttpServletResponse response) {
        log.info("Server response: "
                + "\nRequest URI : " + request.getRequestURI()
                + "\nRequest Method : " + request.getMethod()
                + "\nRequest parameters: " + request.getQueryString()
                + "\nResponse Headers : " + getHeaders(response)
                + "\nResponse Status : " + response.getStatus());
    }

    public static void logClientRequest(HttpRequest request, byte[] body) {
        log.info("Client request: "
                + "\nURI         : " + request.getURI()
                + "\nMethod      : " + request.getMethod()
                + "\nHeaders     : " + request.getHeaders()
                + "\nRequest body: " + new String(body, StandardCharsets.UTF_8));
    }

    public static void logClientResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        log.info("Client response: "
                + "\nRequest URI : " + request.getURI()
                + "\nRequest Method : " + request.getMethod()
                + "\nResponse Headers      : " + response.getHeaders()
                + "\nResponse Status : " + response.getStatusCode()
                + "\nResponse body: " + StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
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
}
