package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.dto.QuestionDto;
import sdu.addd.qasys.entity.Question;

public interface QuestionService extends IService<Question> {

    boolean addQuestion(Question question);

    QuestionDto getQuestion(Long questionId);

    IPage<QuestionDto> listQuestion(PageQuery pageQuery, Long categoryId, Long authorId);

    IPage<QuestionDto> searchQuestion(PageQuery pageQuery, String text, Long categoryId, Long authorId);

    boolean removeQuestion(Long categoryId);

    boolean isAuthor(Long categoryId, Long userId);
}
