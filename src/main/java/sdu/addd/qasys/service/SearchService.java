package sdu.addd.qasys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.entity.Question;

import java.io.IOException;

public interface SearchService {
    /**
     * 将问题存入索引库
     */
    void saveQuestion(Question question) throws IOException;

    /**
     * 将问题从索引库中删除
     */
    void removeQuestion(Long questionId) throws IOException;

    /**
     * 修改问题
     */
    void updateQuestion(Question question) throws IOException;

    /**
     * 获取问题文档
     * 无likeCount字段; 内容为去除Html样式的内容
     */
    Question getQuestionDoc(Long questionId);

    /**
     * 搜索问题
     * @param text 搜索内容
     * @param authorId 指定作者
     * @param categoryId 指定板块
     * @return Post对象，未设置likeCount字段
     */
    IPage<Question> searchQuestions(PageQuery pageQuery, String text, Long authorId, Long categoryId);

    /**
     * 将数据库中所有问题同步到索引库
     */
    void syncAllQuestion();
}
