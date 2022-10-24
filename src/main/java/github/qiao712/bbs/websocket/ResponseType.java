package github.qiao712.bbs.websocket;

public enum ResponseType {
    PRIVATE_MESSAGE,
    KICKED,             //被新的连接挤掉
    ERROR,              //错误提示
}
