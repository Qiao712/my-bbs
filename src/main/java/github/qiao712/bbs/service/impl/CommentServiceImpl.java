package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.dto.message.ReplyMessageContent;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
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
    private PostMapper postMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private MessageService messageService;

    @Override
    public boolean addComment(Comment comment) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        comment.setAuthorId(currentUser.getId());

        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", comment.getPostId());
        if(!postMapper.exists(queryWrapper)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "贴子不存在");
        }

        //若该评论回复一个一级评论
        if(comment.getRepliedId() != null){
            Comment repliedComment = commentMapper.selectById(comment.getRepliedId());
            if(repliedComment == null || !repliedComment.getPostId().equals(comment.getPostId())){
                throw new ServiceException(ResultCode.INVALID_PARAM, "被回复的评论不存在");
            }

            //设置二级评论的parentId
            if(repliedComment.getParentId() != null){
                //被回复的评论为一个二级评论，指向同一个一级评论
                comment.setParentId(repliedComment.getParentId());
            }else{
                //被回复的评论为一个一级评论，指向该一级评论
                comment.setParentId(repliedComment.getId());
            }
        }

        //创建评论，获取主键(插入附加时使用)
        boolean flag = commentMapper.insert(comment) > 0;

        //贴子评论数+1
        postMapper.increaseCommentCount(comment.getPostId(), 1L);

        //发送评论/回复消息
        sendCommentNoticeMessage(comment);

        //标记贴子需要刷新热度值
        statisticsService.markPostToFreshScore(comment.getPostId());

        return flag;
    }

    @Override
    public IPage<CommentDto> listComments(PageQuery pageQuery, Long postId, Long parentCommentId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("post_id", postId);
        queryWrapper.orderByAsc("create_time");
        if(parentCommentId != null){
            //查询父评论id为parentCommentId的所有评论
            queryWrapper.eq("parent_id", parentCommentId);
        }else{
            //查询一级评论
            queryWrapper.isNull("parent_id");
        }
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
            commentDto.setRepliedUserName(authorUsernameMap.get(comment.getRepliedId()));

            //当前用户是否点赞
            commentDto.setLiked(likeService.hasLikedComment(comment.getId(), currentUserId));

            commentDtos.add(commentDto);
        }

        return PageUtil.replaceRecords(commentPage, commentDtos);
    }

    @Override
    public IPage<CommentDetailDto> listCommentsByAuthor(PageQuery pageQuery, Long authorId) {
        return commentMapper.listCommentDetailDtos(pageQuery.getIPage(), authorId);
    }

    @Override
    public boolean removeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if(comment == null) return false;

        List<Long> commentsToDelete = new ArrayList<>();
        if(comment.getParentId() == null){  //该评论为一级评论
            //需要删除的子评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Comment::getId).eq(Comment::getParentId, commentId);
            commentsToDelete = commentMapper.selectList(queryWrapper).stream().map(Comment::getId).collect(Collectors.toList());
        }else{  //二级评论(二级评论无图片)
            //删除回复其的评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Comment::getId).eq(Comment::getRepliedId, commentId);
            commentsToDelete = commentMapper.selectList(queryWrapper).stream().map(Comment::getId).collect(Collectors.toList());
        }
        commentsToDelete.add(commentId);

        //更新评论数量
        postMapper.increaseCommentCount(comment.getPostId(), (long) - commentsToDelete.size());

        //标记贴子需要刷新热度值
        statisticsService.markPostToFreshScore(comment.getPostId());

        //删除提示消息
        messageService.removeMessages(null, null, ReplyMessageContent.class, commentsToDelete.stream().map(String::valueOf).collect(Collectors.toList()));

        return commentMapper.deleteBatchIds(commentsToDelete) > 0;
    }

    @Override
    public boolean isAuthor(Long commentId, Long userId) {
        Comment commentQuery = new Comment();
        commentQuery.setId(commentId);
        commentQuery.setAuthorId(userId);
        return commentMapper.exists(new QueryWrapper<>(commentQuery));
    }

    /**
     * 发送评论提醒消息
     */
    private void sendCommentNoticeMessage(Comment comment){
        ReplyMessageContent messageContent = new ReplyMessageContent();

        messageContent.setCommentId(comment.getId());
        messageContent.setComment(comment.getContent());
        messageContent.setAuthorId(comment.getAuthorId());
        messageContent.setAuthorUsername(userService.getUsername(comment.getAuthorId()));
        messageContent.setPostId(comment.getPostId());

        //设置贴子标题
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getId, comment.getPostId());
        queryWrapper.select(Post::getTitle, Post::getAuthorId);
        Post post = postMapper.selectOne(queryWrapper);
        messageContent.setPostTitle(post.getTitle());

        //消息接收者
        Long receiverId = null;
        if(comment.getRepliedId() != null){
            //接收者为被回复评论的作者
            LambdaQueryWrapper<Comment> commentQueryWrapper = new LambdaQueryWrapper<>();
            commentQueryWrapper.select(Comment::getAuthorId);
            commentQueryWrapper.eq(Comment::getId, comment.getRepliedId());
            Comment repliedComment = commentMapper.selectOne(commentQueryWrapper);
            receiverId = repliedComment.getAuthorId();
        }else{
            //接收者为贴子作者
            receiverId = post.getAuthorId();
        }

        if(!Objects.equals(receiverId, comment.getAuthorId())){
            //使用评论的id作为消息的key，以便快速检索删除
            messageService.sendMessage(comment.getAuthorId(), receiverId, comment.getId().toString(), messageContent);
        }
    }
}
