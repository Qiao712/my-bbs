package github.qiao712.bbs.exception;

import github.qiao712.bbs.domain.base.ResultCode;

public class ServiceException extends RuntimeException{
    private final ResultCode errorCode;

    public ResultCode getErrorCode() {
        return errorCode;
    }

    public ServiceException(ResultCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public ServiceException(ResultCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ServiceException(ResultCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ServiceException(ResultCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public ServiceException(ResultCode errorCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = errorCode;
    }
}
