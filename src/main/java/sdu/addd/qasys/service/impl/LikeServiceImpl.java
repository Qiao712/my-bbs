package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.entity.AnswerLike;
import sdu.addd.qasys.entity.CommentLike;
import sdu.addd.qasys.entity.QuestionLike;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.*;
import sdu.addd.qasys.service.LikeService;
import sdu.addd.qasys.util.SecurityUtil;

@Service
@Slf4j
public class LikeServiceImpl extends ServiceImpl<QuestionLikeMapper, QuestionLike> implements LikeService {
    @Autowired
    private QuestionLikeMapper questionLikeMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private CommentLikeMapper commentLikeMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private AnswerLikeMapper answerLikeMapper;


    //Post-----------------------------------------------------------
    @Override
    @Transactional
    public void likeQuestion(Long questionId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(questionMapper.existsById(questionId))){
            throw new ServiceException(ResultCode.INVALID_PARAM, "贴子不存在");
        }

        if(like){
            if(questionLikeMapper.isQuestionLikedByUser(questionId, userId)){
                throw new ServiceException(ResultCode.INVALID_PARAM, "不可重复点赞");
            }

            //点赞记录
            QuestionLike questionLike = new QuestionLike();
            questionLike.setQuestionId(questionId);
            questionLike.setUserId(userId);
            questionLikeMapper.insert(questionLike);

            questionMapper.increaseAnswerCount(questionId, 1L);
        }else{
            if(!questionLikeMapper.isQuestionLikedByUser(questionId, userId)){
                throw new ServiceException(ResultCode.INVALID_PARAM, "未点赞");
            }

            LambdaQueryWrapper<QuestionLike> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QuestionLike::getUserId, userId);
            queryWrapper.eq(QuestionLike::getQuestionId, questionId);
            questionLikeMapper.delete(queryWrapper);

            questionMapper.increaseAnswerCount(questionId, -1L);
        }
    }

    @Override
    public boolean hasLikedQuestion(Long questionId, Long userId) {
        return questionLikeMapper.isQuestionLikedByUser(questionId, userId);
    }

    //Answer-------------------------------------------------------------
    @Override
    public void likeAnswer(Long answerId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(answerMapper.existsById(answerId))){
            throw new ServiceException(ResultCode.INVALID_PARAM, "回答不存在");
        }

        if(like){
            if(answerLikeMapper.isAnswerLikedByUser(answerId, userId)){
                throw new ServiceException(ResultCode.INVALID_PARAM, "不可重复点赞");
            }

            //点赞记录
            AnswerLike answerLike = new AnswerLike();
            answerLike.setAnswerId(answerId);
            answerLike.setUserId(userId);
            answerLikeMapper.insert(answerLike);

            answerMapper.increaseLikeCount(answerId, 1L);
        }else{
            if(!answerLikeMapper.isAnswerLikedByUser(answerId, userId)){
                throw new ServiceException(ResultCode.INVALID_PARAM, "未点赞");
            }

            LambdaQueryWrapper<AnswerLike> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AnswerLike::getUserId, userId);
            queryWrapper.eq(AnswerLike::getAnswerId, answerId);
            answerLikeMapper.delete(queryWrapper);

            answerMapper.increaseLikeCount(answerId, -1L);
        }
    }

    @Override
    public boolean hasLikedAnswer(Long answerId, Long userId) {
        return answerLikeMapper.isAnswerLikedByUser(answerId, userId);
    }

    //Comment-----------------------------------------------------------
    @Override
    public void likeComment(Long commentId, boolean like) {
        Long userId = SecurityUtil.getCurrentUser().getId();

        if(!Boolean.TRUE.equals(commentMapper.existsById(commentId))){
            throw new ServiceException(ResultCode.INVALID_PARAM, "评论不存在");
        }

        if(like){
            if(commentLikeMapper.isCommentLikedByUser(commentId, userId)){
                throw new ServiceException(ResultCode.INVALID_PARAM, "不可重复点赞");
            }

            //点赞记录
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUserId(userId);
            commentLikeMapper.insert(commentLike);

            commentMapper.increaseLikeCount(commentId, 1L);
        }else{
            if(!commentLikeMapper.isCommentLikedByUser(commentId, userId)){
                throw new ServiceException(ResultCode.INVALID_PARAM,  "未点赞");
            }

            LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CommentLike::getUserId, userId);
            queryWrapper.eq(CommentLike::getCommentId, commentId);
            commentLikeMapper.delete(queryWrapper);

            commentMapper.increaseLikeCount(commentId, -1L);
        }
    }

    @Override
    public boolean hasLikedComment(Long commentId, Long userId) {
        return commentLikeMapper.isCommentLikedByUser(commentId, userId);
    }
}
