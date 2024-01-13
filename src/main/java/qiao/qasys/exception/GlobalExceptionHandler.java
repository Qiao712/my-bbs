package qiao.qasys.exception;

import qiao.qasys.common.Result;
import qiao.qasys.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(value = ServiceException.class)
    public Result<Void> handleServiceException(ServiceException serviceException){
        return Result.fail(serviceException.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result<Void> handleArgumentException(MethodArgumentNotValidException exception){
        StringBuilder message = new StringBuilder("参数无效");
        if(!exception.getFieldErrors().isEmpty()) message.append(':');
        for (FieldError fieldError : exception.getFieldErrors()) {
            message.append(fieldError.getDefaultMessage());
            message.append(';');
        }
        return Result.build(ResultCode.INVALID_PARAM, message.toString());
    }

    @ResponseBody
    @ExceptionHandler(value = Throwable.class)
    public Result<Void> handleAnyException(Throwable e){
        if(e instanceof HttpRequestMethodNotSupportedException){
            return Result.build(ResultCode.METHOD_NOT_SUPPORTED);
        }else if(e instanceof AccessDeniedException){
            return Result.build(ResultCode.NO_PERMISSION, "无权访问");
        }

        //记录未知错误
        log.error(e.getMessage(), e);

        return Result.build(ResultCode.FAILURE, "请求处理失败");
    }
}
