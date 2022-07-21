package io.kennethmartens.ckm.rest.v1.exception;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.Instant;

@Slf4j
public class RestExceptionHandler {

    @ServerExceptionMapper
    public RestResponse<RestExceptionResponse> notFoundExceptionHandling(NotFoundException exception) {
        log.error("Not Found Exception: %1$s", exception);
        return RestResponse.status(RestResponse.Status.NOT_FOUND, buildException(
                RestResponse.Status.NOT_FOUND,
                RestResponse.StatusCode.NOT_FOUND,
                exception.getMessage()
            )
        );
    }

    @ServerExceptionMapper
    public RestResponse<RestExceptionResponse> badRequestException(BadRequestException exception) {
        log.error("Bad Request Exception : %1$s", exception);
        return RestResponse.status(RestResponse.Status.BAD_REQUEST, buildException(
                        RestResponse.Status.BAD_REQUEST,
                        RestResponse.StatusCode.BAD_REQUEST,
                        exception.getMessage()
                )
        );
    }

    private RestExceptionResponse buildException(RestResponse.Status status, Integer statusCode, String message) {
        return RestExceptionResponse.builder()
                .status(status)
                .statusCode(statusCode)
                .timestamp(Instant.now())
                .message(message)
                .build();
    }

}
