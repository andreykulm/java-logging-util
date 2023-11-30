package sa.qiwa.logging.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ErrorModel {

    String name;

    String message;

    @JsonProperty("stack_trace")
    String stackTrace;
}
