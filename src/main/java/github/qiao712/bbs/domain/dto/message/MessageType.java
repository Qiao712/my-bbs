package github.qiao712.bbs.domain.dto.message;

import java.util.HashMap;
import java.util.Map;

public class MessageType {
    private static final Map<String, Class<? extends MessageContent>> classMap = new HashMap<>();
    static {
        classMap.put("private", PrivateMessageContent.class);
        classMap.put("reply", ReplyMessageContent.class);
    }

    public static Class<? extends MessageContent> getMessageContentTypeClass(String typeName){
        return classMap.get(typeName.toLowerCase());
    }
}
