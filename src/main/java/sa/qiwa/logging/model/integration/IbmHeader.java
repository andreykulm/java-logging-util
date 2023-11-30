package sa.qiwa.logging.model.integration;

import java.time.LocalDateTime;

public interface IbmHeader {

    String getTransactionId();

    String getChannelId();

    String getSessionId();

    String getRequestTime();

    String getMwRequestTime();

    String getServiceCode();

}
