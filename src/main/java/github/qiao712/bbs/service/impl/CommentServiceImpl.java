package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.CommentDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.Comment;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.FileMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.service.CommentService;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.PostService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
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

            //TODO: 发送提示消息给被回复者
        }else{
            //TODO:发送提示消息给楼主

            //子评论不允许插入图片
        }

        return commentMapper.insert(comment) > 0;
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

        //组装CommentDto
        List<CommentDto> commentDtos = new ArrayList<>();
        for (Comment comment : comments) {
            CommentDto commentDto = new CommentDto();
            BeanUtils.copyProperties(comment, commentDto);

            //author
            User user = userService.getUser(comment.getAuthorId());
            UserDto userDto = new UserDto();
            BeanUtils.copyProperties(user, userDto);
            commentDto.setAuthor(userDto);

            //user replied
            commentDto.setRepliedUserName(userService.getUsername(comment.getRepliedId()));

            commentDtos.add(commentDto);
        }

        return PageUtil.replaceRecords(commentPage, commentDtos);
    }
}
