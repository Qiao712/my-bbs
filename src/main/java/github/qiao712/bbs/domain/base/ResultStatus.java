package github.qiao712.bbs.domain.base;

public class ResultStatus {
    //成功
    public static int SUCCESS = 0;
    //一般的业务错误
    public static int FAILURE = 1;
    //不支持的请求方法
    public static int METHOD_NOT_SUPPORTED = 2;
    //Token无效
    public static int INVALID_TOKEN = 3;
    //参数非法
    public static int ARGUMENT_NOT_VALID = 4;
    //无权访问
    public static int FORBIDDEN = 403;
    //未知错误
    public static int ERROR = 500;
}
