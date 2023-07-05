package github.qiao712.bbs.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.Question;

import java.io.IOException;

public interface SearchService {
    /**
     * 将贴子存入索引库
     */
    void savePost(Question question) throws IOException;

    /**
     * 将贴子从索引库中删除
     */
    void removePost(Long postId) throws IOException;

    /**
     * 修改贴子
     */
    void updatePost(Question question) throws IOException;

    /**
     * 获取贴子文档
     * 无likeCount字段; 内容为去除Html样式的内容
     */
    Question getPostDoc(Long postId);

    /**
     * 搜索贴子
     * @param text 搜索内容
     * @param authorId 指定作者
     * @param forumId 指定板块
     * @return Post对象，未设置likeCount字段
     */
    IPage<Question> searchPosts(PageQuery pageQuery, String text, Long authorId, Long forumId);

    /**
     * 将数据库中所有贴子同步到索引库
     */
    void syncAllPosts();
}
