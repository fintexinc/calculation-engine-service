package com.fintex.ce.exception;

import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DataErrorException extends GeneralRuntimeException {

    private final String id;
    private final ExceptionCode code;
    private final HttpStatus httpStatus = HttpStatus.OK;

    public DataErrorException(final String message, final String id, final ExceptionCode code) {
        super(message);
        this.id = id;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataErrorException that = (DataErrorException) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (code != that.code) return false;
        if (!getMessage().equals(that.getMessage())) return false;
        return httpStatus == that.httpStatus;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + httpStatus.hashCode();
        result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
        return result;
    }
}
