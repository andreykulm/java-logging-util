package sa.qiwa.logging.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoggingRequestModel {

    @JsonProperty("session_key")
    String sessionKey;

    String method;

    String path;

    Map<String, String> headers;

    Map<String, Object> message;
}
