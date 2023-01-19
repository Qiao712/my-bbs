package github.qiao712.bbs.mq;

import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Post;

public enum MessageType {
    POST_ADD(MQConfig.POST_TOPIC, Post.class),
    POST_DELETE(MQConfig.POST_TOPIC, Long.class),
    POST_UPDATE(MQConfig.POST_TOPIC, Post.class),

    COMMENT_ADD(MQConfig.COMMENT_TOPIC, Comment.class);

    private final Class<?> messageBodyType;
    private final String topic;

    MessageType(String topic, Class<?> messageBodyType) {
        this.messageBodyType = messageBodyType;
        this.topic = topic;
    }

    public Class<?> getMessageBodyType() {
        return messageBodyType;
    }

    public String getTopic() {
        return topic;
    }
}
