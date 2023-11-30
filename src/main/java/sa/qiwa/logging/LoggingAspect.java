package sa.qiwa.logging;

import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.CurrentTraceContext;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;

/**
 * Aspect to add logging to any controller bean, which logs entry/exit from the methods.
 */
@Slf4j
public class LoggingAspect {

    public static final String STARTED = "otel-started";

    private final Tracer tracer;

    private final LoggingProperties loggingProperties;

    private static final String ENTRY = "Intro";
    private static final String EXIT = "Outro";
    private static final String INVOCATION_ID = " - InvocationId:";
    private static final String METHOD = " - Method: ";
    private static final String PARAMS = " - Params: ";
    private static final String EMPTY = "[none]";
    private static final String EXECUTION_TIME = " - Execution Time: ";
    private static final String RETURN = " - Return: ";
    private static final String MS = " (ms)";
    private static final String EXCEPTION = " - Exception: ";
    private static final String DATA_SOURCE = " - DataSource: ";
    private static final String SUCCESS = " - Success: ";
    private static final char VALUE_START = '[';
    private static final char VALUE_END = ']';

    private static final Map<Class<?>, Function<Object, String>> STRING_CONVERTERS =
            ImmutableMap.<Class<?>, Function<Object, String>>builder()
                    .put(byte[].class, value -> "byte[size=" + ((byte[]) value).length + ']')
                    .put(Tuple.class, value -> ((Tuple) value).toSeq().map(LoggingAspect::prepareValue).collect(Collectors.joining(",", "(", ")")))
                    .put(Collection.class, value -> ((Collection<?>) value).size() > FileUtils.ONE_KB / 4
                            ? "Collection" +
                            "[" + ((Collection<?>) value).size() + "]"
                            : ((Collection<?>) value).stream().map(LoggingAspect::prepareValue)
                            .collect(Collectors.joining(",", "[", "]")))
                    .build();

    public LoggingAspect(Tracer tracer, LoggingProperties loggingProperties) {
        this.tracer = tracer;
        this.loggingProperties = loggingProperties;
    }

    public LoggingAspect(Tracer tracer) {
        this.tracer = tracer;
        this.loggingProperties = LoggingProperties.DEFAULT;
    }

    public LoggingAspect(boolean logEnabled) {
        this.tracer = null;
        this.loggingProperties = new LoggingProperties(false, logEnabled);
    }

    /**
     * Logs Entry and Exit for all classes
     *
     * @param joinPoint Default for any AroundAdvice
     * @return the control flow...
     * @throws Throwable if something went wrong that's just thrown. Also logs the duration and exception type, however
     *                   logging exceptions is part of the Exception Handler.
     */

    public Object logEntryExit(ProceedingJoinPoint joinPoint) throws Throwable {

        String currentClassName = getSimpleName(joinPoint);
        String currentMethodName = joinPoint.getSignature().getName();
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());

        if (loggingProperties.logsEnabled && loggingProperties.telemetryEnabled) {
            return logAndMessure(joinPoint, currentClassName, currentMethodName, logger);
        } else if (loggingProperties.logsEnabled) {
            return log(joinPoint, logger, currentClassName);
        } else if (loggingProperties.telemetryEnabled) {
            return messure(joinPoint, currentClassName, currentMethodName);
        } else {
            return joinPoint.proceed();
        }
    }

    private Object messure(ProceedingJoinPoint joinPoint, String currentClassName, String currentMethodName) throws Throwable {
        handleServerTraceName(tracer);
        Span newSpan = tracer.nextSpan().name(currentClassName + "." + currentMethodName);
        try (Tracer.SpanInScope spanInScope = this.tracer.withSpan(newSpan.start())) {
            return joinPoint.proceed();
        } finally {
            newSpan.end();
        }
    }

    private Object log(ProceedingJoinPoint joinPoint, Logger logger, String currentClassName) throws Throwable {
        String uuid = UUID.randomUUID().toString();
        logEntry(logger, uuid, currentClassName, joinPoint);
        Instant start = Instant.now();
        try {
            Object returnValue = joinPoint.proceed();
            logExitInfo(logger, joinPoint, uuid, currentClassName, start, returnValue);
            return returnValue;
        } catch (Throwable throwable) {
            logExitError(logger, joinPoint, uuid, currentClassName, start, throwable);
            throw throwable;
        }
    }

    private Object logAndMessure(ProceedingJoinPoint joinPoint, String currentClassName, String currentMethodName, Logger logger) throws Throwable {
        handleServerTraceName(tracer);
        Span newSpan = tracer.nextSpan().name(currentClassName + "." + currentMethodName);
        try (Tracer.SpanInScope spanInScope = this.tracer.withSpan(newSpan.start())) {
            String uuid = getUuidString();
            logEntry(logger, uuid, currentClassName, joinPoint);
            Instant start = Instant.now();
            try {
                Object returnValue = joinPoint.proceed();
                logExitInfo(logger, joinPoint, uuid, currentClassName, start, returnValue);
                return returnValue;
            } catch (Throwable throwable) {
                logExitError(logger, joinPoint, uuid, currentClassName, start, throwable);
                throw throwable;
            } finally {
                newSpan.end();
            }
        }
    }

    private void logEntry(Logger logger, String uuid, String currentClassName, ProceedingJoinPoint joinPoint) {
        logger.info("{}{}{}{}{}{}{}", ENTRY, uuid, DATA_SOURCE, currentClassName, METHOD, joinPoint, getParamString(joinPoint));
    }

    private void logExitInfo(Logger logger, ProceedingJoinPoint joinPoint, String uuid, String currentClassName, Instant start, Object returnValue) {
        logger.info("{}{}{}{}{}{}{}{}}{}{}{}", EXIT, uuid, DATA_SOURCE, currentClassName, SUCCESS, true, METHOD,
                joinPoint, getExecutionTime(start, Instant.now()), MS, getReturnString(returnValue));
    }

    private void logExitError(Logger logger, ProceedingJoinPoint joinPoint, String uuid, String currentClassName, Instant start, Throwable throwable) {
        logger.error("{}{}{}{}{}{}}{}{}{}{}{}", EXIT, uuid, DATA_SOURCE, currentClassName, SUCCESS, false, METHOD,
                joinPoint, getExecutionTime(start, Instant.now()), MS, getExceptionString(throwable));
    }

    private void handleServerTraceName(Tracer tracer) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            Object started = request.getAttribute(STARTED);
            if (started != null) {
                return;
            }
            Object attribute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
            if (attribute != null) {
                Span span = tracer.currentSpan();
                if (span != null) {
                    span.name(request.getMethod() + " " + attribute);
                    request.setAttribute(STARTED, true);
                }
            }
        }
    }

    private static String getSimpleName(ProceedingJoinPoint joinPoint) {
        try {
            String[] split = joinPoint.getSignature().getDeclaringTypeName().split("\\.");
            return split[split.length - 1];
        } catch (Exception ignored) {
        }
        return joinPoint.getTarget().getClass().getSimpleName();
    }

    /**
     * Convenience method to get the string representation of the param values.
     *
     * @param joinPoint The upcoming joinPoint to get the Args from it.
     * @return The .toString of each argument concatenated with a comma in between.
     */
    private String getParamString(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return PARAMS + EMPTY;
        } else {
            return PARAMS + wrapValue(prepareValues(args));
        }
    }

    /**
     * Generate a random UUID with prefix, so that log statements are correlated
     *
     * @return String representation of a UUID for logging.
     */
    private String getUuidString() {
        CurrentTraceContext currentTraceContext = tracer.currentTraceContext() != null ? tracer.currentTraceContext() : null;
        TraceContext context = currentTraceContext != null ? currentTraceContext.context() : null;
        String traceId = context != null ? context.traceId() : UUID.randomUUID().toString();
        return INVOCATION_ID + traceId;
    }

    /**
     * Convenience method to get the string representation of the execution time
     *
     * @param start  The instant before method execution
     * @param finish The instant after method execution
     * @return String representation of the execution time
     */
    private String getExecutionTime(Instant start, Instant finish) {
        Duration duration = Duration.between(start, finish);
        return EXECUTION_TIME + duration.toMillis();
    }

    /**
     * Convenience method to de-nastify the Amazon DynamoDB List classes lack of .toString() method.
     *
     * @param returnObject the object that we would like to have a proper .toString() from.
     * @return the .toString() method on the object, if it's Paginated*List, then we first stream it into a
     * java.util.List to get a proper toString()
     */
    private String getReturnString(Object returnObject) {
        return RETURN + wrapValue(prepareValue(returnObject));
    }

    private String wrapValue(String returnObject) {
        return VALUE_START + returnObject + VALUE_END;
    }

    private static String prepareValue(Object value) {
        if (value == null) {
            return EMPTY;
        } else {
            String preparedValue = STRING_CONVERTERS.entrySet().stream()
                    .filter(converter -> converter.getKey().isAssignableFrom(value.getClass()))
                    .findAny()
                    .map(Map.Entry::getValue)
                    .orElse(Object::toString)
                    .apply(value);
            return preparedValue.length() > FileUtils.ONE_KB * 2 ? "toString[" + preparedValue.length() + ']' : preparedValue;
        }
    }

    private static String prepareValues(Object... value) {
        return Arrays.stream(value).map(LoggingAspect::prepareValue).collect(Collectors.joining(","));
    }

    private String getExceptionString(Throwable exception) {
        return EXCEPTION + wrapValue(prepareValue(exception));
    }
}