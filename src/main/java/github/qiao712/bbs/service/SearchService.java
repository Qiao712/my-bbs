package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Question;

public interface SearchService {
    /**
     * 将问题存入索引库
     */
    void saveQuestion(Question question);

    /**
     * 将问题从索引库中删除
     */
    void removeQuestion(Long questionId);

    /**
     * 修改问题
     */
    void updateQuestion(Question question);

    /**
     * 获取问题文档
     * 无likeCount字段; 内容为去除Html样式的内容
     */
    Question getQuestionDoc(Long questionId);

    /**
     * 搜索问题
     * @param text 搜索内容
     * @param authorId 指定作者
     * @param forumId 指定板块
     * @return Question对象，未设置likeCount字段
     */
    IPage<Question> searchQuestions(PageQuery pageQuery, String text, Long authorId, Long forumId);

    /**
     * 将数据库中所有问题同步到索引库
     */
    void syncAllQuestions();
}
