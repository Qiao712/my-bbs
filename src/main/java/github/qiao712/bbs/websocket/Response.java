package github.qiao712.bbs.websocket;

import github.qiao712.bbs.domain.dto.PrivateMessageDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向客户端返回消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {
    private int responseType;

    //只有当requestType为PRIVATE_MESSAGE时携带
    private PrivateMessageDto privateMessage;
}
