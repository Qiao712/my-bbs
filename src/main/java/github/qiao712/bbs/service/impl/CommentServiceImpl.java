package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mq.comment.CommentMessageSender;
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

@Service("commentService")
@Transactional
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private PostMapper postMapper;
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
    private CommentMessageSender commentMessageSender;

    @Override
    public boolean addComment(Comment comment) {
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        comment.setAuthorId(currentUser.getId());

        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", comment.getPostId());
        if(!postMapper.exists(queryWrapper)){
            throw new ServiceException("贴子不存在");
        }

        //若该评论回复一个一级评论
        if(comment.getRepliedId() != null){
            Comment repliedComment = commentMapper.selectById(comment.getRepliedId());
            if(repliedComment == null || !repliedComment.getPostId().equals(comment.getPostId())){
                throw new ServiceException("被回复的评论不存在");
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
                throw new ServiceException("图片数量超出限制");
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
        postMapper.increaseCommentCount(comment.getPostId(), 1L);

        //发送评论/回复消息
        commentMessageSender.sendCommentAddMessage(comment);

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
    public IPage<CommentDetailDto> listCommentsByAuthor(PageQuery pageQuery, String authorUsername) {
        Long authorId = null;
        if(authorUsername != null){
            authorId = userService.getUserIdByUsername(authorUsername);
            if(authorId == null){
                //未找到用户，返回空的页
                return pageQuery.<CommentDetailDto>getIPage().setTotal(0);
            }
        }
        return commentMapper.listCommentDetailDtos(pageQuery.getIPage(), authorId);
    }

    @Override
    public boolean removeComment(Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if(comment == null) return false;
        long deletedCommentCount = 0;

        if(comment.getParentId() == null){
            //该评论为一级评论
            //删除子评论
            Comment commentQuery = new Comment();
            commentQuery.setParentId(commentId);
            deletedCommentCount += commentMapper.delete(new QueryWrapper<>(commentQuery));

            //引用计数减
            List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfComment(comment.getPostId(), comment.getId());
            if(!attachmentFileIds.isEmpty())
                fileService.increaseReferenceCount(attachmentFileIds, -1);

            //删除attachment记录
            Attachment attachmentQuery = new Attachment();
            attachmentQuery.setPostId(comment.getPostId());
            attachmentQuery.setCommentId(comment.getId());
            attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));
        }else{
            //二级评论(二级评论无需)
            //删除回复其的评论
            Comment commentQuery = new Comment();
            commentQuery.setRepliedId(commentId);
            deletedCommentCount += commentMapper.delete(new QueryWrapper<>(commentQuery));
        }

        //更新评论数量
        postMapper.increaseCommentCount(comment.getPostId(), -deletedCommentCount - 1);

        //标记贴子需要刷新热度值
        statisticsService.markPostToFreshScore(comment.getPostId());

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
