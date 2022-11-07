package github.qiao712.bbs.mq.post;

import com.alibaba.fastjson.JSON;
import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Post;
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
public class PostMessageSender {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    PostMessageListener postMessageListener;

    public void sendPostAddMessage(Post post){
        PostMessage postAddMessage = PostMessage.buildPostAddMessage(post);
        sendMessage(post.getId(), postAddMessage);
    }

    public void sendPostUpdateMessage(Post post){
        PostMessage postUpdateMessage = PostMessage.buildPostUpdateMessage(post);
        sendMessage(post.getId(), postUpdateMessage);
    }

    public void sendPostDeleteMessage(Long postId){
        PostMessage postDeleteMessage = PostMessage.buildPostDeleteMessage(postId);
        sendMessage(postId, postDeleteMessage);
    }

    /**
     * 向POST的主题发送消息
     */
    private void sendMessage(Long postId, PostMessage postMessage){
        //使用PostId做为Key，使同一个Post的各种操作的消息进入同一个partition，保证对一个Post的操作的有序性
        ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(MQConfig.POST_TOPIC, postId.toString(), JSON.toJSONString(postMessage));

        //发送失败，则在本地进行处理
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(@NotNull Throwable ex) {
                log.warn("贴子相关消息发送失败, 直接在本地进行处理", ex);

                postMessageListener.processMessage(postMessage);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
            }
        });
    }
}
