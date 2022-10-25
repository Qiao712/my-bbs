package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.QuestionDto;
import github.qiao712.bbs.domain.entity.Question;
import org.springframework.web.multipart.MultipartFile;

public interface QuestionService extends IService<Question> {

    boolean addQuestion(Question question);

    /**
     * 上传问题中插入的图片
     * @return 图片的url
     */
    String uploadImage(MultipartFile image);

    QuestionDto getQuestion(Long questionId);

    IPage<QuestionDto> listQuestion(PageQuery pageQuery, Long forumId, String authorUsername);

    IPage<QuestionDto> searchQuestions(PageQuery pageQuery, String text, Long forumId, Long authorId);

    boolean removeQuestion(Long questionId);

    boolean isAuthor(Long questionId, Long userId);
}
