package sa.qiwa.logging.loggback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.loggingevent.StackTraceJsonProvider;
import sa.qiwa.logging.model.ErrorModel;

import java.io.IOException;

public class QiwaStackTraceProvider extends StackTraceJsonProvider {

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            var err = new ErrorModel(throwableProxy.getClassName(),
                    throwableProxy.getMessage(),
                    getThrowableConverter().convert(event));
            generator.writePOJOField("error", err);
        }
    }

}
