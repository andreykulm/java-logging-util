package sa.qiwa.logging.loggback;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import net.logstash.logback.composite.AbstractJsonProvider;
import sa.qiwa.logging.model.IntegrationLoggingDataHolder;
import sa.qiwa.logging.model.integration.IntegrationLogModel;

import java.io.IOException;
import java.util.Map;

public class QiwaIntegrationJsonProvider extends AbstractJsonProvider<DeferredProcessingAware> {

    public static ObjectMapper mapper = new ObjectMapper();

    @Override
    public void writeTo(JsonGenerator generator, DeferredProcessingAware deferredProcessingAware) throws IOException {
        if (IntegrationLoggingDataHolder.transaction.get() != null) {
            IntegrationLogModel pojo = IntegrationLoggingDataHolder.transaction.get();
            var map = mapper.convertValue(pojo, new TypeReference<Map<String, Object>>() {
            });
            map.forEach((k, v) -> {
                writeStr(generator, k, v);
            });
        }
    }

    @SneakyThrows
    public void writeStr(JsonGenerator generator, String filedName, Object value) {
        if (value != null) {
            generator.writePOJOField(filedName, value);
        }
    }

}
