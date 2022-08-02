package github.qiao712.bbs.exception;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.base.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
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
    @ExceptionHandler(value = Throwable.class)
    public Result<Void> handleAnyException(Throwable e){
        if(e instanceof HttpRequestMethodNotSupportedException){
            return Result.build(ResultStatus.METHOD_NOT_SUPPORTED, "不支持的Http方法");
        }else if(e instanceof AccessDeniedException){
            return Result.build(ResultStatus.FORBIDDEN, "无权访问");
        }

        log.error(e.getMessage(), e);

        return Result.build(ResultStatus.ERROR, "请求处理失败");
    }
}
