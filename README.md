# java-logging-util

Logging instrumental for java

# Components

1. <strong>LoggingRequestFilter</strong> - Incoming server Request/Response logging
2. <strong>LoggingRequestInterceptor</strong> - Outgoing client Request/Response logging
3. <strong>LoggingAspect</strong> - Service/Controller/Repository level logging and telemetry

## LoggingRequestFilter
### How to use
1. Import to you project.
2. Declare a bean

```java
import org.springframework.context.annotation.Configuration;
import sa.qiwa.logging.LoggingRequestFilter;

@Configuration
public class Config {
    @Bean
    public LoggingRequestFilter loggingRequestFilter(){
        return new LoggingRequestFilter();
    }
}
```
## LoggingRequestInterceptor
### How to use
1. Import to you project.
2. Modify rest template. <strong>Important -  to not break app you need to use BufferingClientHttpRequestFactory</strong>

```java
import org.springframework.context.annotation.Configuration;
import sa.qiwa.logging.LoggingRequestFilter;

@Configuration
public  class Config {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());
        builder = builder.requestFactory(() -> factory);

        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(TIMEOUT))
                .setReadTimeout(Duration.ofSeconds(TIMEOUT))
                .build();
        restTemplate.setInterceptors(List.of(new LoggingRequestInterceptor()));
        return restTemplate;
    }
}
```


## LoggingAspect
### How to use
1. Import to you project.
2. Import spring sleuth
##### pom.xml
```xml

   <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-sleuth-otel-dependencies</artifactId>
                <version>1.1.2</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```
```xml
   <dependencies>
    <!-- OTEL -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-sleuth</artifactId>
        <exclusions>
            <exclusion>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-sleuth-brave</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-sleuth-otel-autoconfigure</artifactId>
    </dependency>
   </dependencies>
```
3. Configure spring-cloud-sleuth
##### application.yaml
```yaml
spring:
  sleuth:
    otel:
      exporter:
        otlp:
          enabled: true
          endpoint: http://otel-collector:4317
      config:
        trace-id-ratio-based: 1.0
```

4. Create Your LoggingAspect calls and extend existing LoggingAspect
```java
@Aspect
@Component
public class CustomLoggingAspect extends LoggingAspect {

    public CustomLoggingAspect(Tracer tracer) {
        super(tracer, LoggingProperties.DEFAULT);
    }

    @Override
    @Around(value = """
            execution(public * com.takamol.qiwa.dynamicgatewayapi.controller.*.*(..)) ||
            execution(public * com.takamol.qiwa.dynamicgatewayapi.service.*.*(..)) ||
            execution(public * com.takamol.qiwa.dynamicgatewayapi.repository.*.*(..)) ||
            execution(* org.springframework.data.jpa.repository.JpaRepository+.*(..))))
            """)
    public Object logEntryExit(ProceedingJoinPoint joinPoint) throws Throwable {
        return super.logEntryExit(joinPoint);
    }
}
```
5. Run app and check logs

