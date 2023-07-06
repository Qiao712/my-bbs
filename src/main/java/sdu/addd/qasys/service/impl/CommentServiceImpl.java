package sdu.addd.qasys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import sdu.addd.qasys.common.PageQuery;
import sdu.addd.qasys.common.ResultCode;
import sdu.addd.qasys.dto.AuthUser;
import sdu.addd.qasys.dto.UserDto;
import sdu.addd.qasys.entity.Answer;
import sdu.addd.qasys.entity.User;
import sdu.addd.qasys.exception.ServiceException;
import sdu.addd.qasys.mapper.AnswerMapper;
import sdu.addd.qasys.mapper.CommentMapper;
import sdu.addd.qasys.service.LikeService;
import sdu.addd.qasys.service.UserService;
import sdu.addd.qasys.util.PageUtil;
import sdu.addd.qasys.util.SecurityUtil;
import sdu.addd.qasys.dto.CommentDto;
import sdu.addd.qasys.entity.Comment;
import sdu.addd.qasys.service.CommentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @Override
    public boolean addComment(Comment comment) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        comment.setAuthorId(currentUser.getId());

        QueryWrapper<Answer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", comment.getAnswerId());
        if(!answerMapper.exists(queryWrapper)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "回答不存在");
        }

        //若该评论回复另一个评论,校验
        if(comment.getRepliedId() != null){
            Comment repliedComment = commentMapper.selectById(comment.getRepliedId());
            if(repliedComment == null || !repliedComment.getAnswerId().equals(comment.getAnswerId())){
                throw new ServiceException(ResultCode.INVALID_PARAM, "被回复的评论不存在");
            }
        }

        //创建评论，获取主键(插入附加时使用)
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public IPage<CommentDto> listComments(PageQuery pageQuery, Long answerId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getAnswerId, answerId);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        IPage<Comment> commentPage = commentMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<Comment> comments = commentPage.getRecords();

        //将所用到的用户信息一次查出
        Set<Long> authorIds = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserDto> userDtoMap = new HashMap<>(authorIds.size());
        for (Long authorId : authorIds) {
            User user = userService.getUser(authorId);
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            userDtoMap.put(authorId, userDto);
        }

        //comment id --> author username map
        Map<Long, String> authorUsernameMap = new HashMap<>(authorIds.size());
        for (Comment comment : comments) {
            UserDto author = userDtoMap.get(comment.getAuthorId());
            authorUsernameMap.put(comment.getId(), author != null ? author.getUsername() : null);
        }

        //当前用户ID，用于判断是否已点赞
        Long currentUserId = SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null;

        //组装CommentDto
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = new CommentDto();
            BeanUtils.copyProperties(comment, commentDto);

            //author
            commentDto.setAuthor(userDtoMap.get(comment.getAuthorId()));

            //user replied
            Long repliedId = comment.getRepliedId();
            if(repliedId != null){
                String repliedUser = authorUsernameMap.get(repliedId);
                commentDto.setRepliedUserName(repliedUser != null ? repliedUser : "[评论已被删除]");
            }

            //当前用户是否点赞
            commentDto.setLiked(likeService.hasLikedComment(comment.getId(), currentUserId));

            commentDtos.add(commentDto);
        }

        return PageUtil.replaceRecords(commentPage, commentDtos);
    }

    @Override
    public boolean removeComment(Long commentId) {
        return commentMapper.deleteById(commentId) > 0;
    }

    @Override
    public boolean isAuthor(Long commentId, Long userId) {
        Comment commentQuery = new Comment();
        commentQuery.setId(commentId);
        commentQuery.setAuthorId(userId);
        return commentMapper.exists(new QueryWrapper<>(commentQuery));
    }
}
