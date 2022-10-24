package github.qiao712.bbs.websocket;

import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import lombok.Data;

/**
 * 通过WebSocket发来的请求
 */
@Data
public class Request {
    private int requestType;

    //只有当requestType为PRIVATE_MESSAGE时携带
    private PrivateMessageDto privateMessage;
}
