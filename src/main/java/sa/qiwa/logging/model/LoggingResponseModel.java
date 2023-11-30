package sa.qiwa.logging.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoggingResponseModel {

    String status;

    boolean success;

    Map<String, String> headers;

    Map<String, Object> message;
}
