package us.onnasoft.ayanami.exceptions;

import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse extends RuntimeException {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    private final int status;
    private final String message;
    private final String path;

    @JsonIgnore
    private final Throwable throwable;

    public ApiErrorResponse(final LocalDateTime timestamp, final int status, final String message, final String path) {
        super(message);
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.throwable = null;
    }

    public ApiErrorResponse(final String path, final Exception e) {
        super(e.getMessage(), e);
        if (e instanceof ResponseStatusException rse) {
            this.timestamp = LocalDateTime.now();
            this.status = rse.getStatusCode().value();
            this.message = rse.getReason();
            this.path = path;
            this.throwable = null;
            return;
        }
        this.timestamp = LocalDateTime.now();
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.message = e.getMessage();
        this.path = path;
        this.throwable = e;
    }

    @Override
    @JsonIgnore
    public StackTraceElement[] getStackTrace() {
        return new StackTraceElement[0];
    }

    @Override
    @JsonIgnore
    public synchronized Throwable getCause() {
        return null;
    }

    @Override
    @JsonIgnore
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public Throwable getThrowable() {
        return null;
    }
}