package sa.qiwa.logging.loggback;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;
import sa.qiwa.logging.model.LoggingDataHolder;

import java.io.IOException;

public class QiwaJsonProvider extends AbstractJsonProvider<DeferredProcessingAware> {
    @Override
    public void writeTo(JsonGenerator generator, DeferredProcessingAware deferredProcessingAware) throws IOException {
        if (LoggingDataHolder.request.get() != null) {
            generator.writePOJOField("request", LoggingDataHolder.request.get());
        }
        if (LoggingDataHolder.response.get() != null) {
            generator.writePOJOField("response", LoggingDataHolder.response.get());
        }
        if (LoggingDataHolder.started.get() != null) {
            generator.writeStringField("started_at", LoggingDataHolder.started.get().toString());
        }
        if (LoggingDataHolder.protocol.get() != null) {
            generator.writeStringField("protocol", LoggingDataHolder.protocol.get());
        }
        if (LoggingDataHolder.finished.get() != null) {
            generator.writeStringField("finished_at", LoggingDataHolder.finished.get().toString());
        }
        if (LoggingDataHolder.elapsed.get() != null) {
            generator.writeNumberField("elapsed_ms", LoggingDataHolder.elapsed.get().toMillis());
        }
    }

}
