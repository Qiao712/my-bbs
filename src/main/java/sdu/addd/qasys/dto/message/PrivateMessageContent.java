package sdu.addd.qasys.dto.message;

import lombok.Data;

/**
 * 用户间私信消息
 */
@Data
public class PrivateMessageContent implements MessageContent{
    private String text;
}
