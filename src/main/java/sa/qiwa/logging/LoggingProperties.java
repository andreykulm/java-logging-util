package sa.qiwa.logging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoggingProperties {

    public static LoggingProperties ENABLED = new LoggingProperties(true, true);

    public static LoggingProperties DISABLED = new LoggingProperties(false, false);

    public static LoggingProperties DEFAULT = ENABLED;

    boolean telemetryEnabled;

    boolean logsEnabled;


}
