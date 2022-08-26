package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.entity.CommentLike;
import github.qiao712.bbs.domain.entity.PostLike;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.CommentLikeMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostLikeMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.CommentService;
import github.qiao712.bbs.service.LikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.service.StatisticsService;
import github.qiao712.bbs.util.SecurityUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class LikeServiceImpl extends ServiceImpl<PostLikeMapper, PostLike> implements LikeService {
    @Autowired
    private PostLikeMapper postLikeMapper;
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private StatisticsService statisticsService;

    @Override
    @Transactional
    public boolean likePost(Long postId) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);

        if(postLikeMapper.isPostLikedByUser(postId, userId)){
            throw new ServiceException("不可重复点赞");
        }

        //标记需要更新贴子热度分值
        statisticsService.markPostToFreshScore(postId);

        return postMapper.increaseLikeCount(postId, 1L) > 0 && postLikeMapper.insert(postLike) > 0;
    }

    @Override
    @Transactional
    public boolean undoLikePost(Long postId) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);

        if(!postLikeMapper.isPostLikedByUser(postId, userId)){
            throw new ServiceException("未点赞");
        }

        //标记需要更新贴子热度分值
        statisticsService.markPostToFreshScore(postId);

        return postMapper.increaseLikeCount(postId, -1L) > 0 && postLikeMapper.delete(new QueryWrapper<>(postLike)) > 0;
    }

    @Override
    public boolean hasLikedPost(Long postId, Long userId) {
        return postLikeMapper.isPostLikedByUser(postId, userId);
    }

    @Override
    @Transactional
    public boolean likeComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);

        if(commentLikeMapper.isCommentLikedByUser(commentId, userId)){
            throw new ServiceException("不可重复点赞");
        }

        return commentMapper.increaseLikeCount(commentId, 1L) > 0 && commentLikeMapper.insert(commentLike) > 0;
    }

    @Override
    @Transactional
    public boolean undoLikeComment(Long commentId) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        CommentLike postLike = new CommentLike();
        postLike.setCommentId(commentId);
        postLike.setUserId(userId);

        if(!commentLikeMapper.isCommentLikedByUser(commentId, userId)){
            throw new ServiceException("未点赞");
        }

        return commentMapper.increaseLikeCount(commentId, -1L) > 0 && commentLikeMapper.delete(new QueryWrapper<>(postLike)) > 0;
    }

    @Override
    public boolean hasLikedComment(Long commentId, Long userId) {
        return commentLikeMapper.isCommentLikedByUser(commentId, userId);
    }
}
