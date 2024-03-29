package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.dto.message.ReplyMessageContent;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mq.MessageSender;
import github.qiao712.bbs.mq.MessageType;
import github.qiao712.bbs.service.*;
import github.qiao712.bbs.util.HtmlUtil;
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
    private PostService postService;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private MessageSender messageSender;
    @Autowired
    private MessageService messageService;

    @Override
    public boolean addComment(Comment comment) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        comment.setAuthorId(currentUser.getId());

        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getId, comment.getPostId());
        if(!postService.getBaseMapper().exists(queryWrapper)){
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

        //若为一级评论，则允许插入图片(附件)
        if(comment.getRepliedId() == null){
            //解析出引用的图片
            List<String> urls = HtmlUtil.getImageUrls(comment.getContent());
            if(urls.size() > systemConfig.getMaxCommentImageNum()){
                throw new ServiceException(ResultCode.COMMENT_ERROR, "图片数量超出限制");
            }

            //如果文件的上传者是该用户(评论作者)，则记录该评论对图片的引用(记录为该评论一个附件)
            List<Long> imageFileIds = new ArrayList<>(urls.size());
            for (String url : urls) {
                FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
                if(imageFileIdentity == null) continue;  //为外部连接

                if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId())
                    && FileService.POST_IMAGE_FILE.equals(imageFileIdentity.getSource())){
                    imageFileIds.add(imageFileIdentity.getId());
                }
            }
            if(!imageFileIds.isEmpty()){
                attachmentMapper.insertAttachments(comment.getPostId(), comment.getId(), imageFileIds);
                //引用图片
                fileService.increaseReferenceCount(imageFileIds, 1);
            }
        }

        //贴子评论数+1
        postService.increaseCommentCount(comment.getPostId(), 1L);

        //发送评论/回复消息
        messageSender.sendMessageSync(MessageType.COMMENT_ADD, comment.getId().toString(), comment);

        //标记贴子需要刷新热度值
        statisticsService.markPostToFreshScore(comment.getPostId());
        return flag;
    }

    @Override
    public IPage<CommentDto> listComments(PageQuery pageQuery, Long postId, Long parentCommentId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getPostId, postId);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        if(parentCommentId != null){
            //查询父评论id为parentCommentId的所有评论
            queryWrapper.eq(Comment::getParentId, parentCommentId);
        }else{
            //查询一级评论
            queryWrapper.isNull(Comment::getParentId);
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

            //图片等文件的引用计数减
            List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfComment(comment.getPostId(), comment.getId());
            if(!attachmentFileIds.isEmpty())
                fileService.increaseReferenceCount(attachmentFileIds, -1);

            //删除attachment记录
            Attachment attachmentQuery = new Attachment();
            attachmentQuery.setPostId(comment.getPostId());
            attachmentQuery.setCommentId(comment.getId());
            attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));
        }else{  //二级评论(二级评论无图片)
            //删除回复其的评论
            LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Comment::getId).eq(Comment::getRepliedId, commentId);
            commentsToDelete = commentMapper.selectList(queryWrapper).stream().map(Comment::getId).collect(Collectors.toList());
        }
        commentsToDelete.add(commentId);

        //更新评论数量
        postService.increaseCommentCount(comment.getPostId(), (long) - commentsToDelete.size());

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
}
