package github.qiao712.bbs.domain.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 同一返回对象
 */
@Data
@Builder
@AllArgsConstructor
public class Result<T> {
    private int status;
    private String message;
    private T data;

    public static Result<Void> succeed(){
        return succeed("");
    }

    public static <T> Result<T> succeed(T data){
        return succeed("", data);
    }

    public static <T> Result<T> succeed(String message){
        return succeed(message, null);
    }

    public static <T> Result<T> succeed(String message, T data){
        return new Result<>(ResultStatus.SUCCESS, message, data);
    }

    public static Result<Void> fail(){
        return fail("");
    }

    public static Result<Void> fail(String message){
        return new Result<>(ResultStatus.FAILURE, message, null);
    }

    public static Result<Void> build(int status, String message){
        return new Result<>(status, message, null);
    }

    public static Result<Void> build(boolean isSuccessful){
        return isSuccessful ? succeed() : fail();
    }
}
