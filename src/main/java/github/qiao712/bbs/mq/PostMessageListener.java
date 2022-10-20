package github.qiao712.bbs.mq;

import github.qiao712.bbs.config.MQConfig;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Post相关事件消息的监听器
 */
@Component
public class PostMessageListener {
    @Autowired
    private SearchService searchService;

    /**
     * 贴子添加消息
     * 存入ElasticSearch中
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConfig.POST_ADD_QUEUE),
            exchange = @Exchange(name = MQConfig.POST_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = {MQConfig.POST_ADD_QUEUE}
    ))
    public void listenPostAdd(Post post){
        searchService.savePost(post);
    }

    /**
     * 贴子更新
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConfig.POST_UPDATE_QUEUE),
            exchange = @Exchange(name = MQConfig.POST_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = {MQConfig.POST_UPDATE_QUEUE}
    ))
    public void listenPostUpdate(Post post){
        searchService.updatePost(post);
    }

    /**
     * 贴子删除
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConfig.POST_DELETE_QUEUE),
            exchange = @Exchange(name = MQConfig.POST_EXCHANGE, type = ExchangeTypes.DIRECT),
            key = {MQConfig.POST_DELETE_QUEUE}
    ))
    public void listenPostDelete(Long postId){
        searchService.removePost(postId);
    }
}
