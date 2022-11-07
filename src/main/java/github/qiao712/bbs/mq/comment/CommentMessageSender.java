package github.qiao712.bbs.mq.comment;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Comment;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class CommentMessageSender {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private CommentMessageListener commentMessageListener;

    public void sendCommentAddMessage(Comment comment){
        CommentMessage commentMessage = CommentMessage.buildCommentAddMessage(comment);
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(MQConfig.COMMENT_TOPIC, comment.getId().toString(), JSON.toJSONString(commentMessage));

        //发送失败，则在本地进行处理
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(@NotNull Throwable ex) {
                log.warn("贴子相关消息发送失败, 直接在本地进行处理", ex);

                commentMessageListener.processMessage(commentMessage);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
            }
        });
    }
}
