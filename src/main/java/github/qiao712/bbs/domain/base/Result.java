package github.qiao712.bbs.domain.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 统一返回对象
 */
@Data
@Builder
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public Result(ResultCode code, String message, T data){
        this.code = code.getCode();
        this.message = message;
        this.data = data;
    }

    public Result(ResultCode code, T data){
        this.code = code.getCode();
        this.message = code.getDefaultMessage();
        this.data = data;
    }

    public static Result<Void> succeed(){
        return new Result<>(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> succeed(T data){
        return new Result<>(ResultCode.SUCCESS, data);
    }

    public static Result<Void> succeed(String message){
        return new Result<>(ResultCode.SUCCESS, message, null);
    }

    public static <T> Result<T> succeed(String message, T data){
        return new Result<>(ResultCode.SUCCESS, message, data);
    }

    public static <T> Result<T> succeedNotNull(T data){
        if(data != null){
            return new Result<>(ResultCode.SUCCESS, data);
        }else{
            return new Result<>(ResultCode.NOT_EXIST, null);
        }
    }

    public static Result<Void> fail(){
        return new Result<>(ResultCode.FAILURE, null);
    }

    public static Result<Void> fail(String message){
        return new Result<>(ResultCode.FAILURE, message, null);
    }

    public static Result<Void> build(ResultCode code){
        return new Result<>(code, null);
    }

    public static Result<Void> build(ResultCode code, String message){
        return new Result<>(code, message, null);
    }

    public static Result<Void> build(boolean isSuccessful){
        return isSuccessful ? succeed() : fail();
    }
}
