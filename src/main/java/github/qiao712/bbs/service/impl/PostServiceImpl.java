package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.dto.UserDto;
import github.qiao712.bbs.domain.entity.*;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AttachmentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.ForumService;
import github.qiao712.bbs.service.PostService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.FileUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
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
        List<FileIdentity> pictures = findPictureUsedInPost(post.getContent());

        //如果文件的上传者是该该用户(贴子作者)，则记录该贴子对图片的引用(记录为该贴子的一个附件)
        List<Long> pictureIds = new ArrayList<>(pictures.size());
        for (FileIdentity picture : pictures) {
            if(Objects.equals(picture.getUploaderId(), currentUser.getId())){
                pictureIds.add(picture.getId());
            }
        }
        if(!pictureIds.isEmpty()){
            attachmentMapper.insertAttachments(post.getId(), pictureIds);

            //将引用的图片文件标记为非临时文件，不再进行清理
            fileService.setTempFlags(pictureIds, false);
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

    private final static Pattern pattern = Pattern.compile("<img.*? src=\"");    //匹配开头位置，再寻找下一个引号    .*加? 非贪心地匹配

    /**
     * 找出贴子中引用的图片的文件
     * @param html
     * @return
     */
    private List<FileIdentity> findPictureUsedInPost(String html){
        List<FileIdentity> fileIdentities = new ArrayList<>();

        //遍历img标签中的src属性的内容
        Matcher matcher = pattern.matcher(html);
        int start, end;
        while(matcher.find()){
            start = matcher.end();  //<img ... src=" 的结束即连接地址的开始
            end = html.indexOf('\"', start);
            String url = html.substring(start, end);

            //url --> id
            FileIdentity fileIdentity = fileService.getFileIdentityByUrl(url);
            if(fileIdentity != null){
                fileIdentities.add(fileIdentity);
            }
        }

        return fileIdentities;
    }
}
