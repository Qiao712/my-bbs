package github.qiao712.bbs.event;

import github.qiao712.bbs.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostEventListener implements ApplicationListener<PostEvent> {
    @Autowired
    private SearchService searchService;

    //异步处理事件，将贴子同步至ElasticSearch
    @Async
    @Override
    public void onApplicationEvent(PostEvent event) {
        log.debug("event: {}", event);

        switch (event.getPostEventType()){
            case CREATE: {
                searchService.savePost(event.getPost());
                break;
            }

            case UPDATE:{
                searchService.updatePost(event.getPost());
                break;
            }

            case DELETE:{
                searchService.removePost(event.getPostId());
                break;
            }
        }
    }
}
