package sdu.addd.qasys.common;

public enum ResultCode {
    //成功
    SUCCESS(0, "操作成功"),

    //通用错误
    FAILURE(10000, "操作错误"),
    INVALID_PARAM(10001, "非法请求参数"),
    NO_PERMISSION(10002, "无权限"),
    NOT_EXIST(10003, "目标实体不存在"),
    METHOD_NOT_SUPPORTED(10004, "不支持的请求方法"),

    //用户服务错误
    USER_ERROR(20000, "用户操作错误"),
    ROLE_ERROR(20001, "角色操作错误"),
    LOGIN_ERROR(20002, "登录失败"),
    FOLLOW_ERROR(20003, "关注失败"),

    //贴子服务错误
    COMMENT_ERROR(30001, "评论操作错误"),
    QUESTION_ERROR(30002, "贴子操作错误"),

    //消息服务错误
    MESSAGE_ERROR(40001, "消息错误"),

    //文件服务错误
    FILE_ERROR(50001, "文件操作错误"),
    UPLOAD_ERROR(50002, "上传失败"),
    ;

    /**
     * 错误码
     */
    private final int code;

    /**
     * 默认消息
     */
    private final String defaultMessage;

    ResultCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
