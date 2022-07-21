package io.kennethmartens.ckm.rest.v1.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class RestExceptionResponse {
    private Instant timestamp;
    private RestResponse.Status status;
    private Integer statusCode;
    private String message;
}
