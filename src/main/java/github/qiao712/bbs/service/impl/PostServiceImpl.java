package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.ForumService;
import github.qiao712.bbs.service.PostService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.FileUtil;
import github.qiao712.bbs.util.HtmlUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private ForumService forumService;
    @Autowired
    private FileService fileService;
    @Autowired
    private AttachmentMapper attachmentMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SystemConfig systemConfig;

    @Override
    @Transactional
    public boolean addPost(Post post) {
        //设置作者id
        AuthUser currentUser = SecurityUtil.getCurrentUser();
        post.setAuthorId(currentUser.getId());

        if(postMapper.insert(post) == 0){
            return false;
        }

        //解析出引用的图片
        List<String> urls = HtmlUtil.getImageUrls(post.getContent());

        //如果文件的上传者是该该用户(贴子作者)，则记录该贴子对图片的引用(记录为该贴子的一个附件)
        List<Long> imageFileIds = new ArrayList<>(urls.size());
        for (String url : urls) {
            FileIdentity imageFileIdentity = fileService.getFileIdentityByUrl(url);
            if(imageFileIdentity == null) continue;  //为外部连接

            if(Objects.equals(imageFileIdentity.getUploaderId(), currentUser.getId())){
                imageFileIds.add(imageFileIdentity.getId());
            }
        }
        if(!imageFileIds.isEmpty()){
            attachmentMapper.insertAttachments(post.getId(), null, imageFileIds);

            //将引用的图片文件标记为非临时文件，不再进行清理
            fileService.setTempFlags(imageFileIds, false);
        }

        return true;
    }

    @Override
    @Transactional
    public String uploadPicture(MultipartFile picture) {
        //检查文件大小限制
        if(picture.getSize() > systemConfig.getMaxPostPictureSize()){
            throw new ServiceException("文件大小超过" + systemConfig.getMaxPostPictureSize() + "bytes");
        }
        if(!FileUtil.isPictureFile(picture.getOriginalFilename())){
            throw new ServiceException("文件非图片类型");
        }

        //上传为临时文件
        FileIdentity fileIdentity = fileService.uploadFile("post_picture", picture, true);

        if(fileIdentity != null){
            return fileService.getFileUrl(fileIdentity.getId());
        }else{
            return null;
        }
    }

    @Override
    public PostDto getPost(Long postId) {
        Post post = postMapper.selectById(postId);
        return postDtoMap(post);
    }

    @Override
    public IPage<PostDto> listPosts(PageQuery pageQuery, Long forumId) {
        Post post = new Post();
        post.setForumId(forumId);
        IPage<Post> postPage = postMapper.selectPage(pageQuery.getIPage(), new QueryWrapper<>(post));

        List<Post> posts = postPage.getRecords();
        List<PostDto> postDtos = posts.stream().map(this::postDtoMap).collect(Collectors.toList());
        return PageUtil.replaceRecords(postPage, postDtos);
    }

    @Override
    public boolean removePost(Long postId) {
        //删除所有附件
        //标记其引用的图片(附件)可以清理
        List<Long> attachmentFileIds = attachmentMapper.selectAttachmentFileIdsOfPost(postId);
        if(!attachmentFileIds.isEmpty())
            fileService.setTempFlags(attachmentFileIds, true);
        //删除attachment记录
        Attachment attachmentQuery = new Attachment();
        attachmentQuery.setPostId(postId);
        attachmentMapper.delete(new QueryWrapper<>(attachmentQuery));

        //删除所有评论
        //将repliedId、parentId设为null，防止外键阻止删除
        UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("replied_id", null);
        updateWrapper.set("parent_id", null);
        updateWrapper.eq("post_id", postId);
        commentMapper.update(null, updateWrapper);
        //删除
        Comment commentQuery = new Comment();
        commentQuery.setPostId(postId);
        commentMapper.delete(new QueryWrapper<>(commentQuery));

        return postMapper.deleteById(postId) > 0;
    }

    @Override
    public boolean isAuthor(Long postId, Long userId) {
        Post postQuery = new Post();
        postQuery.setId(postId);
        postQuery.setAuthorId(userId);
        return postMapper.exists(new QueryWrapper<>(postQuery));
    }

    private PostDto postDtoMap(Post post){
        if(post == null) return null;
        PostDto postDto = new PostDto();
        BeanUtils.copyProperties(post, postDto);

        //作者用户信息
        User user = userService.getUser(post.getAuthorId());
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        postDto.setAuthor(userDto);

        //板块名称
        Forum forum = forumService.getById(post.getForumId());
        postDto.setForumName(forum.getName());
        return postDto;
    }
}
