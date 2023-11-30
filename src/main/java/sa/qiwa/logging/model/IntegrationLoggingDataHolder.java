package sa.qiwa.logging.model;

import lombok.experimental.UtilityClass;
import sa.qiwa.logging.model.integration.IntegrationLogModel;

import java.time.Duration;
import java.time.Instant;

@UtilityClass
public class IntegrationLoggingDataHolder {

    public static final ThreadLocal<IntegrationLogModel> transaction = new ThreadLocal<>();

    public static void clear() {
        transaction.remove();
    }

}
