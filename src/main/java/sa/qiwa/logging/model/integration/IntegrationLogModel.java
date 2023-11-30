package sa.qiwa.logging.model.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IntegrationLogModel implements Serializable {


    /**
     * Field from IBM header<br/>
     * Example: QIWA<br/>
     * Mandatory: NO
     */
    @JsonProperty("channel_id")
    String channelId;


    /**
     * What integration type does your service use<br/>
     * Example: IBM<br/>
     * Mandatory: YES
     */
    @NotBlank
    @JsonProperty("type")
    String type;

    /**
     * Integration URL<br/>
     * Example: https://gw-apic.qiwa.info/takamol/staging/invoicesmanagement/updatevatnumber <br/>
     * Mandatory: YES
     */
    @NotBlank
    @JsonProperty("endpoint")
    String endpoint;

    /**
     * HTTP methods if it is simple integration or SOAP method if it is old IBM | HRSD integration<br/>
     * Example: GET|POST|DELETE, JSON-API or  SOAP <br/>
     * Mandatory: YES
     */
    @NotBlank
    @JsonProperty("method")
    String method;

    /**
     * Title of the service that initiated the request<br/>
     * Example: working-permits-debts <br/>
     * Mandatory: YES
     */
    @NotBlank
    @JsonProperty("creator")
    String creator;

    /**
     * The trace ID is used to search all traces and join spans for a single request. If no trace context is specified in the request, and trace is enabled, a random trace ID is generated for all trace spans<br/>
     * Example: 4bf92f3577b34da6a3ce929d0e0e4736 <br/>
     * Mandatory: NO
     */
    @JsonProperty("trace_id")
    String traceId;

    /**
     * Personal number of the person who initiated the request <br/>
     * Example: 1092608123 <br/>
     * Mandatory: NO
     */
    @JsonProperty("personal_number")
    String personalNumber;

    /**
     * Elapsed time between a client request and the server response (ms) <br/>
     * Example: 542.468516 <br/>
     * Mandatory: YES
     */
    @NotNull
    @JsonProperty("duration")
    Long duration;

    /**
     * Full request that the service sends to the integration point <br/>
     * Example: {request body} <br/>
     * Mandatory: YES
     */
    @JsonProperty("service_request")
    @NotBlank
    String serviceRequest;

    /**
     * Full response from the integration point  <br/>
     * Example: {response body} <br/>
     * Mandatory: YES
     */
    @JsonProperty("service_response")
    @NotBlank
    String serviceResponse;

    /**
     * Network state from service to integration point <br/>
     * Example: ok <br/>
     * Mandatory: NO
     */
    @JsonProperty("network_status")
    String networkStatus;

    /**
     * Network error between service and integration point <br/>
     * Example: No route to host <br/>
     * Mandatory: NO
     */
    @JsonProperty("network_error")
    String networkError;

    /**
     * HTTP code from response <br/>
     * Example: 200 <br/>
     * Mandatory: NO
     */
    @JsonProperty("http_code")
    int httpCode;

    /**
     * HTTP status from response <br/>
     * Example: ok <br/>
     * Mandatory: NO
     */
    @JsonProperty("http_status")
    String httpStatus;

    /**
     * Result of the request from integration point <br/>
     * Example: ERROR <br/>
     * Mandatory: NO
     */
    @JsonProperty("service_response_status")
    String serviceResponseStatus;

    /**
     * Response code from integration point <br/>
     * Example: NGCI0001 <br/>
     * Mandatory: NO
     */
    @JsonProperty("service_response_code")
    String serviceResponseCode;

    /**
     * Text interpretation of an error in English <br/>
     * Example: Supplied Birth date is not correct <br/>
     * Mandatory: NO
     */
    @JsonProperty("service_response_errors_en")
    String serviceResponseErrorsEn;

    /**
     * Text interpretation of an error in Arabic <br/>
     * Example: تاريخ الميلاد المدخل غير صحيح <br/>
     * Mandatory: NO
     */
    @JsonProperty("service_response_errors_ar")
    String serviceResponseErrorsAr;


    /**
     * Is the request successful according to service logic<br/>
     * Example: true <br/>
     * Mandatory: YES
     */
    @JsonProperty("status")
    @NotBlank
    boolean status;

    /**
     * Errors from integration point according to service logic <br/>
     * Example: Error <br/>
     * Mandatory: NO
     */
    @JsonProperty("errors")
    String errors;

    /**
     * ID number of a company involved in this request and response <br/>
     * Example: 11-1998805 <br/>
     * Mandatory: NO
     */
    @JsonProperty("establishment_number")
    String establishmentNumber;

    /**
     * additional data. Not indexed <br/>
     * Example: {field1: "test", field2: 1, field3: "test2", fieldn: n } <br/>
     * Mandatory: NO
     */
    @JsonProperty("extra_fields")
    Map<String, String> extraFields;

    /**
     * Service code for IBM integration <br/>
     * Example: MGLI0001 <br/>
     * Mandatory: NO
     */
    @JsonProperty("service_code")
    String serviceCode;

    /**
     * Response header from your integration point <br/>
     * Example: {headers json} <br/>
     * Mandatory: NO
     */
    @JsonProperty("response_header")
    String responseHeader;


}
