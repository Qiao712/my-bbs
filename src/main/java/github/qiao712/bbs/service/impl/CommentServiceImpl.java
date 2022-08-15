package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDetailDto;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.ForumMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.CommentService;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.UserService;
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
    private PostMapper postMapper;
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private UserService userService;
    @Autowired
    private AttachmentMapper attachmentMapper;

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

        boolean flag = commentMapper.insert(comment) > 0;

        //若为一级评论，则允许插入图片(附件)
        if(comment.getRepliedId() == null){
            //解析出引用的图片
            List<String> urls = HtmlUtil.getImageUrls(comment.getContent());
            //如果文件的上传者是该用户(评论作者)，则记录该评论对图片的引用(记录为该评论一个附件)
            List<Long> imageFileIds = new ArrayList<>(urls.size());
            for (String url : urls) {
                FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
                if(imageFileIdentity == null) continue;  //为外部连接

                if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId())){
                    imageFileIds.add(imageFileIdentity.getId());
                }
            }
            if(!imageFileIds.isEmpty()){
                attachmentMapper.insertAttachments(comment.getPostId(), comment.getId(), imageFileIds);
                //将引用的图片文件标记为非临时文件，不再进行清理
                fileService.setTempFlags(imageFileIds, false);
            }
        }

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

        //comment id --> author
        Map<Long, String> authorUsernameMap = new HashMap<>(authorIds.size());
        for (Comment comment : comments) {
            UserDto author = userDtoMap.get(comment.getAuthorId());
            authorUsernameMap.put(comment.getId(), author != null ? author.getUsername() : null);
        }

        //组装CommentDto
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = new CommentDto();
            BeanUtils.copyProperties(comment, commentDto);

            //author
            commentDto.setAuthor(userDtoMap.get(comment.getAuthorId()));

            //user replied
            commentDto.setRepliedUserName(authorUsernameMap.get(comment.getRepliedId()));

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

        if(comment.getParentId() == null){
            //该评论为一级评论
            //删除子评论
            Comment commentQuery = new Comment();
            commentQuery.setParentId(commentId);
            commentMapper.delete(new QueryWrapper<>(commentQuery));

            //标记其引用的图片(附件)可以清理
            List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfComment(comment.getPostId(), comment.getId());
            if(!attachmentFileIds.isEmpty())
                fileService.setTempFlags(attachmentFileIds, true);

            //删除attachment记录
            Attachment attachmentQuery = new Attachment();
            attachmentQuery.setPostId(comment.getPostId());
            attachmentQuery.setCommentId(comment.getId());
            attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));
        }else{
            //二级评论
            //删除回复其的评论
            Comment commentQuery = new Comment();
            commentQuery.setRepliedId(commentId);
            commentMapper.delete(new QueryWrapper<>(commentQuery));
        }

        return commentMapper.deleteById(commentId) > 0;
    }

    @Override
    public boolean isAuthor(Long commentId, Long userId) {
        Comment commentQuery = new Comment();
        commentQuery.setId(commentId);
        commentQuery.setAuthorId(userId);
        return commentMapper.exists(new QueryWrapper<>(commentQuery));
    }

//    private CommentDetailDto convertToCommentDetailDto(Comment comment){
//        CommentDetailDto commentDetailDto = new CommentDetailDto();
//
//        commentDetailDto.setRepliedUserName( userService.getUsername(comment.get));
//
//        QueryWrapper<Post> postQueryWrapper = new QueryWrapper<>();
//        postQueryWrapper.select("title");
//        postQueryWrapper.select("forum_id");
//        postQueryWrapper.eq("id", comment.getPostId());
//        Post post = postMapper.selectOne(postQueryWrapper);
//        commentDetailDto.setPostTitle(post.getTitle());
//
//        QueryWrapper<Forum> forumQueryWrapper = new QueryWrapper<>();
//        forumQueryWrapper.select("name");
//        forumQueryWrapper.eq("id", post.getForumId());
//        Forum forum = forumMapper.selectOne(forumQueryWrapper);
//        commentDetailDto.setForumName(forum.getName());
//
//        return commentDetailDto;
//    }
}
