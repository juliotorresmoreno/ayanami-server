package us.onnasoft.ayanami.exceptions;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ApiErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ApiErrorResponse(final String path, final Exception e) {
        if (e instanceof ResponseStatusException) {
            final ResponseStatusException rse = (ResponseStatusException) e;
            this.timestamp = LocalDateTime.now();
            this.status = rse.getStatusCode().value();
            this.error = rse.getReason();
            this.message = rse.getReason();
            this.path = path;
            return;
        }
        this.timestamp = LocalDateTime.now();
        this.status = HttpStatus.INTERNAL_SERVER_ERROR.value();
        this.error = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
        this.message = e.getMessage();
        this.path = path;
    }
}