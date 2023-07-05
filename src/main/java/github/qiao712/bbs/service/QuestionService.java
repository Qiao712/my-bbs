package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Question;

public interface QuestionService extends IService<Question> {

    boolean addQuestion(Question question);

    PostDto getQuestion(Long postId);

    IPage<PostDto> listQuestion(PageQuery pageQuery, Long forumId, Long authorId);

    IPage<PostDto> searchQuestion(PageQuery pageQuery, String text, Long forumId, Long authorId);

    boolean removeQuestion(Long postId);

    boolean isAuthor(Long postId, Long userId);
}
