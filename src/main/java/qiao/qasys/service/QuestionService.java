package qiao.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import qiao.qasys.common.PageQuery;
import qiao.qasys.dto.QuestionDto;
import qiao.qasys.entity.Question;

public interface QuestionService extends IService<Question> {

    boolean addQuestion(Question question);

    QuestionDto getQuestion(Long questionId);

    IPage<QuestionDto> listQuestion(PageQuery pageQuery, Long tagId, Long authorId);

    IPage<QuestionDto> searchQuestion(PageQuery pageQuery, String text, Long tagId, Long authorId);

    boolean removeQuestion(Long questionId);

    boolean isAuthor(Long categoryId, Long userId);
}
