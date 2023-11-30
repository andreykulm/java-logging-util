package sa.qiwa.logging.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import sa.qiwa.logging.model.IntegrationLoggingDataHolder;
import sa.qiwa.logging.model.integration.*;

import java.net.SocketException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

public class IntegrationLogUtil {

    public static final String INTEGRATION_LOGGER_NAME = "json_integration";
    public static final String IBM = "IBM";

    public static ObjectMapper mapper = new ObjectMapper();

    public static final Logger integrationLoger = LoggerFactory.getLogger(INTEGRATION_LOGGER_NAME);

    public static void log(IntegrationLogModel model) {
        IntegrationLoggingDataHolder.transaction.set(model);
        try {
            integrationLoger.info("Integration request response logging");
        } finally {
            IntegrationLoggingDataHolder.clear();
        }
    }

    @SneakyThrows
    public static <T extends IbmResponseRoot> Either<Tuple2<HttpStatusCodeException, IntegrationLogModel>, Tuple2<ResponseEntity<T>, IntegrationLogModel>>
    wrapCall(Supplier<ResponseEntity<T>> call, IntegrationLogModel.IntegrationLogModelBuilder builder, IbmRequestRoot req) {
        builder = requestParams(builder, req);
        Instant startTime = Instant.now();
        try {
            ResponseEntity<T> response = call.get();
            Duration duration = Duration.between(startTime, Instant.now());
            builder = builder.status(response.getStatusCode().is2xxSuccessful())
                    .duration(duration.toMillis())
                    .networkStatus("OK")
                    .httpCode(response.getStatusCode().value());
            IbmResponseRoot body = response.getBody();
            builder = builder.responseHeader(mapper.writeValueAsString(response.getHeaders().toSingleValueMap()));
            if (body != null) {
                builder = responseParams(builder, body);
            }
            IntegrationLogModel model = builder.build();
            log(model);
            return Either.right(Tuple.of(response, model));
        } catch (HttpStatusCodeException httError) {
            Duration duration = Duration.between(startTime, Instant.now());
            builder = builder.status(false)
                    .httpCode(httError.getRawStatusCode())
                    .duration(duration.toMillis())
                    .serviceResponse(httError.getResponseBodyAsString());
            if (httError.getResponseHeaders() != null) {
                builder = builder.responseHeader(mapper.writeValueAsString(httError.getResponseHeaders().toSingleValueMap()));
            }
            if (httError.getCause() instanceof SocketException se) {
                builder = builder.networkStatus("SocketException")
                        .networkError(se.getMessage());
            } else {
                builder = builder.networkStatus("OK");
            }
            IntegrationLogModel model = builder.build();
            log(model);
            return Either.left(Tuple.of(httError, model));
        }
    }

    @SneakyThrows
    public static IntegrationLogModel.IntegrationLogModelBuilder requestParams(IntegrationLogModel.IntegrationLogModelBuilder builder, IbmRequestRoot reqRoot) {

        IbmRequestHeader header = reqRoot.getPayload().getHeader();
        return builder
                .type(IBM)
                .method(HttpMethod.POST.name())
                .channelId(header.getChannelId())
                .personalNumber(header.getUserInfo().getIdNumber())
                .serviceRequest(mapper.writeValueAsString(reqRoot))
                .serviceCode(header.getServiceCode());
    }

    @SneakyThrows
    public static IntegrationLogModel.IntegrationLogModelBuilder responseParams(IntegrationLogModel.IntegrationLogModelBuilder builder, IbmResponseRoot resRoot) {

        IbmResponseStatus status = resRoot.getPayload().getHeader().getResponseStatus();
        return builder
                .serviceResponse(mapper.writeValueAsString(resRoot))
                .serviceResponseCode(status.getCode())
                .serviceResponseStatus(status.getStatus())
                .serviceResponseErrorsAr(status.getArabicMsg())
                .serviceResponseErrorsEn(status.getEnglishMsg());
    }
}
