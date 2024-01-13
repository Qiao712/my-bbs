package qiao.qasys.exception;

import qiao.qasys.common.ResultCode;

public class ServiceException extends RuntimeException{
    private final int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public ServiceException(ResultCode resultCode) {
        super(resultCode.getDefaultMessage());
        this.errorCode = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, String message) {
        super(message);
        this.errorCode = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, Throwable cause) {
        super(cause);
        this.errorCode = resultCode.getCode();
    }

    public ServiceException(ResultCode resultCode, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.errorCode = resultCode.getCode();
    }
}
