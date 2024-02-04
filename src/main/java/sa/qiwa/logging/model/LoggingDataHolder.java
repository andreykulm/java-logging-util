package sa.qiwa.logging.model;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.Instant;

@UtilityClass
public class LoggingDataHolder {

    public static final ThreadLocal<LoggingRequestModel> request = new ThreadLocal<>();
    public static final ThreadLocal<LoggingResponseModel> response = new ThreadLocal<>();
    public static final ThreadLocal<String> protocol = new ThreadLocal<>();
    public static final ThreadLocal<Instant> started = new ThreadLocal<>();
    public static final ThreadLocal<Instant> finished = new ThreadLocal<>();
    public static final ThreadLocal<Duration> elapsed = new ThreadLocal<>();
    public static final ThreadLocal<String> requesterIp = new ThreadLocal<>();


    public static void clear() {
        request.remove();
        response.remove();
        started.remove();
        finished.remove();
        elapsed.remove();
        requesterIp.remove();
    }

}
