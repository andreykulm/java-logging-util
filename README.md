# java-logging-util

Logging instrumental for java<br>
According to the following spec:
https://employeesgate.atlassian.net/wiki/spaces/P2H/pages/4218748995/Logs+Format+and+Structure


# Application logs 


### Components
1. <strong>LoggingRequestFilter</strong> - Incoming server Request/Response logging
2. <strong>LoggingRequestInterceptor</strong> - Outgoing client Request/Response logging
3. <strong>LoggingAspect</strong> - Service/Controller/Repository level logging and telemetry


### 1. LoggingRequestFilter (Inbound traffic)
#### 1.1. Import lib to you project.<br>
pom.xml
```xml
...
  <dependency>
    <groupId>sa.qiwa.logging</groupId>
    <artifactId>java-logging-util</artifactId>
    <version>2.0.2-SNAPSHOT</version>
</dependency>
...

<repositories>
    <repository>
        <id>gitlab-maven-logging-lib</id>
        <url>https://gitlab.qiwa.tech/api/v4/projects/1295/packages/maven</url>
    </repository>
</repositories>
...
```
project settings.xml<br>
```xml
...
    <server>
        <id>gitlab-maven-logging-lib</id>
        <configuration>
            <httpHeaders>
                <property>
                    <name>Job-Token</name>
                    <value>${env.CI_JOB_TOKEN}</value>
                </property>
            </httpHeaders>
        </configuration>
    </server>
...
</servers>
```
local settings.xml<br>
```xml
...
    <server>
        <id>gitlab-maven-logging-lib</id>
        <configuration>
            <httpHeaders>
                <property>
                    <name>Private-Token</name>
                    <value>your private token (look gitlab docks how to generate it) </value>
                </property>
            </httpHeaders>
        </configuration>
    </server>
...
</servers>
```
#### 1.2. Declare a bean

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
#### 1.3. Change logback config file (preferred) or copy and add it your project<br>
application.yaml
```yaml
logging:
  config: classpath:logback-qiwa-spring.xml
```
### 2.LoggingRequestInterceptor (outbound traffic)
<strong> (Create separate rest templates for app and integration logs)</strong> <br>
#### 2.1. Import to you project. (check previous steps)<br>
#### 2.2. Modify rest template. <br>
<strong>Important - inorder not break app - you need to use BufferingClientHttpRequestFactory</strong>

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
#### 2.3. Change logback config file (preferred) or copy and add it your project<br>
   application.yaml
```yaml
logging:
  config: classpath:logback-qiwa-spring.xml
```


## 3. LoggingAspect (Verbose logging + telemetry) (optional)(not integrated with common log format)
### How to use
#### 3.1. Import to you project.(check previous steps)
#### 3.2. Import spring sleuth
##### pom.xml
```xml

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2021.0.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
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
#### 3.3. Configure spring-cloud-sleuth
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

#### 3.4. Create Your LoggingAspect,  extend existing LoggingAspect
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
#### 3.5. Run app and check logs


# Integration logs<br>

### Components

1. <strong>IntegrationLogModel</strong> - Main dto which represents integration transaction
2. <strong>IntegrationLogUtil</strong> - Set of util methods which helps with logging

## Description 
Since integrayion logs has a complex structure we have two way of integration 
1. Manual - where <strong>IntegrationLogModel</strong> fulfilled by developer in code and that just logged into logging system
2. Semi Automatic - where only required fields populated by developer and main part of fields are full filed by lib

## 1. Manual 
#### 1.1. Import to you project. (check previous steps)<br>
#### 1.2. Fulfill <strong>IntegrationLogModel</strong> object (check java doc or confluence page for details)
```java
IntegrationLogModel model = IntegrationLogModel.builder()
        ...
        .creator("dynamic-payment-gateway")
        .endpoint(makeSecurePaymentUrl)
        .method("POST")
        ...
        .build();

IntegrationLogUtil.log(model);
```
#### 1.3. Change logback config file (preferred) or copy and add it your project<br>
   application.yaml
```yaml
logging:
  config: classpath:logback-qiwa-spring.xml
```
## 2. Semi Automatic
#### 2.1. Import to you project. (check previous steps)<br>
#### 2.2. IBM models should implement logging interfaces Implement
Request interfaces : IbmRequestRoot -> IbmRequest -> IbmRequestHeader -> IbmUserInfo <br>
Response interfaces : IbmResponseRoot -> IbmResponse -> IbmResponseHeader -> IbmResponseStatus <br>
Request model example: 
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakePaymentRequest implements IbmRequestRoot {

	@JsonProperty("MakePaymentv2Rq")
	private MakePaymentRq payload;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MakePaymentRq implements IbmRequest {

		@JsonProperty("Header")
		private Header header;

		@JsonProperty("Body")
		private MakePaymentRequestBody body;
	}

}
```
Response model example
```java
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MakePaymentResponse extends InitPaymentResponse implements IbmResponseRoot {

    @JsonProperty("MakePaymentv2Rs")
    private MakePaymentRs payload;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MakePaymentRs implements IbmResponse {

        @JsonProperty("Header")
        private Header header;

        @JsonProperty("Body")
        private MakePaymentResponseBody body;
    }
}
```
#### 2.3.1. Full fill additional fields in IntegrationLogModelBuilder
#### 2.3.2. Wrap rest template call to ibm with IntegrationLogUtil.wrapCal
```java

IntegrationLogModelBuilder tranBuilder = IntegrationLogModel.builder()
        .creator("dynamic-payment-gateway")
        .endpoint(makeSecurePaymentUrl)
        .method("POST");//Build basic builder with specific fileds
var result = IntegrationLogUtil.wrapCall(
        () -> restTemplate.postForEntity(makeSecurePaymentUrl, requestEntity, MakePaymentResponse.class),
        tranBuilder, securePaymentRequest); //Execute call
var resultValue = result.getOrElseThrow(l -> l._1); // throw exception if any has been caught 
var makeSecurePaymentResponseEntity = resultValue._1;
```
## 3.Integration log example 
```json
{
  "service_version": "1",
  "time": "2023-11-27T18:27:35.979+0000",
  "thread_name": "http-nio-8081-exec-3",
  "level": "INFO",
  "message": "Integration request response logging",
  "logger_name": "json_integration",
  "channel_id": "Qiwa",
  "type": "IBM",
  "endpoint": "https://gw-apic.qiwa.info/takamol/staging/payment/v2/makepayment",
  "method": "POST",
  "creator": "dynamic-payment-gateway",
  "trace_id": "4bdaee24f94992b9f8944f9ab20786c2",
  "personal_number": "1097473092",
  "duration": 1468,
  "service_request": "{\"MakePaymentv2Rq\":{\"Header\":{\"TransactionId\":\"1701109647938\",\"ChannelId\":\"Qiwa\",\"SessionId\":\"212\",\"RequestTime\":\"2023-11-27 12:27:27.938\",\"MWRequestTime\":\"2023-11-27 12:27:27.938\",\"ServiceCode\":\"MP000001\",\"DebugFlag\":0,\"UserInfo\":{\"UserId\":\"1847184\",\"IDNumber\":\"1097473092\"}},\"Body\":{\"ApplicationId\":\"1\",\"BrandType\":\"VISA\",\"ProductId\":1,\"ProviderId\":1,\"Description\":\"something here\",\"Amount\":\"99.99\",\"Currency\":\"SAR\",\"PGPaymentId\":10651,\"Card\":{\"Holder\":\"Jane Jones\",\"Number\":\"4111111111111111\",\"ExpiryMonth\":\"05\",\"ExpiryYear\":\"2034\",\"CVV\":\"123\"},\"ShopperResultUrl\":\"https://dynamic-gateway-api.qiwa.info/10651/checkout\",\"Customer\":{\"IP\":\"127.0.0.3\",\"Browser\":{\"AcceptHeader\":\"text/html\",\"ScreenColorDepth\":\"48\",\"JavaEnabled\":\"false\",\"Language\":\"de\",\"ScreenHeight\":\"1200\",\"ScreenWidth\":\"1600\",\"Timezone\":\"60\",\"ChallengeWindow\":\"4\",\"UserAgent\":\"Mozilla/4.0 (MSIE 6.0; Windows NT 5.0)\"}}}}}",
  "service_response": "{\"MakePaymentv2Rs\":{\"Header\":{\"TransactionId\":\"1701109647938\",\"ChannelId\":\"Qiwa\",\"SessionId\":\"212\",\"RequestTime\":\"2023-11-27 12:27:27.938\",\"MWResponseTime\":\"2023-11-27 18:27:29.415\",\"ServiceCode\":\"MP000001\",\"DebugFlag\":0,\"ResponseStatus\":{\"Status\":\"PENDING\",\"Code\":\"HYPP0031\",\"ArabicMsg\":\"transaction pending\",\"EnglishMsg\":\"transaction pending\"}},\"Body\":{\"HYPPResponse\":{\"id\":\"8ac7a4a08c1052b1018c12094dc43780\",\"paymentType\":\"DB\",\"paymentBrand\":\"VISA\",\"merchantTransactionId\":\"QW-EV-HP-23-392087FED1-000000000046647\",\"result\":{\"code\":\"000.200.000\",\"description\":\"transaction pending\"},\"resultDetails\":{\"clearingInstituteName\":\"SAIB MPGS\"},\"card\":{\"bin\":411111,\"last4Digits\":1111,\"holder\":\"Jane Jones\",\"expiryMonth\":5,\"expiryYear\":2034},\"redirect\":{\"url\":\"https://test.oppwa.com/connectors/asyncresponse;jsessionid=82E1772B8E4AE46FB1B4FFDCC048DF8B.uat01-vm-con03?asyncsource=MPGS&type=authenticate_payer&ndcid=8ac7a4c768cca59e016906f38be45f5e_8a4bab1a6a21424295043898241a10a2\",\"parameters\":{\"Item\":null}},\"customer\":{\"IP\":null,\"Browser\":null},\"risk\":{\"score\":\"100\"},\"buildNumber\":\"3991bdf112fb62e208d5ec70cd98435b5afb1ed0@2023-11-22 00:52:28 +0000\",\"timestamp\":\"2023-11-27 18:27:29+0000\",\"ndc\":\"8ac7a4c768cca59e016906f38be45f5e_8a4bab1a6a21424295043898241a10a2\"},\"PaymentReference\":\"QW-EV-HP-23-392087FED1-000000000046647\",\"PGPaymentId\":\"10651\"}},\"status_code\":0,\"payment_id\":null}",
  "network_status": "OK",
  "http_code": 201,
  "service_response_status": "PENDING",
  "service_response_code": "HYPP0031",
  "service_response_errors_en": "transaction pending",
  "service_response_errors_ar": "transaction pending",
  "status": true,
  "service_code": "MP000001",
  "response_header": "{\"Date\":\"Mon, 27 Nov 2023 18:27:29 GMT\",\"Content-Type\":\"application/json; charset=utf-8\",\"Connection\":\"keep-alive\",\"X-RateLimit-Limit\":\"name=default,100;\",\"X-RateLimit-Remaining\":\"name=default,92;\",\"Strict-Transport-Security\":\"max-age=15724800; includeSubDomains\",\"Set-Cookie\":\"TS0155db1f=0157efebc164aa11450d4b8cdd205293793f9fce63b17114e8edf65ab0dc07e942d90ed54301d650f0ea2e88e47a59979bf5e00ad2; Path=/; Domain=.gw-apic.qiwa.info\",\"Transfer-Encoding\":\"chunked\"}",
  "service_name": "dynamic-gateway-api",
  "source": "STDOUT",
  "span_id": "554592b4460d119a"
}
```

