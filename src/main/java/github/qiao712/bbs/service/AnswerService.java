package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AnswerDetailDto;
import github.qiao712.bbs.domain.dto.AnswerDto;
import github.qiao712.bbs.domain.entity.Answer;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AnswerService extends IService<Answer> {
    /**
     * 添加回答
     */
    boolean addAnswer(Answer answer);

    /**
     * 删除回答
     */
    boolean removeAnswer(Long answerId);

    /**
     * 获取回答列表
     */
    IPage<AnswerDto> listAnswers(PageQuery pageQuery, Long questionId);

    /**
     * 通过作者获取回答
     */
    IPage<AnswerDetailDto> listAnswersByAuthor(PageQuery pageQuery, String authorUsername);

    /**
     * 判断用户是否为回答的发布者
     */
    boolean isAuthor(Long answerId, Long userId);
}
