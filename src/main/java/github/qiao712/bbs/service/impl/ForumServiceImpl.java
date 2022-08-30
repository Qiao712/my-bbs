package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.domain.entity.Forum;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.ForumMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.ForumService;
import github.qiao712.bbs.util.FileUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private FileService fileService;

    //论坛logo文件source标识
    private final static String FORUM_LOGO_SOURCE = "forum-logo";

    @Override
    public Forum getForum(Long forumId) {
        Forum forum = forumMapper.selectById(forumId);
        setLogoUrl(forum);
        return forum;
    }

    @Override
    public IPage<Forum> listForums(PageQuery pageQuery, Forum condition) {
        QueryWrapper<Forum> queryWrapper = new QueryWrapper<>();
        if(Strings.isNotBlank(condition.getName())){
            queryWrapper.like("name", '%' + condition.getName() + '%');
        }
        if(Strings.isNotBlank(condition.getCategory())){
            queryWrapper.eq("category", condition.getCategory());
        }

        IPage<Forum> forumPage = forumMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        forumPage.getRecords().forEach(this::setLogoUrl);

        return forumPage;
    }

    @Override
    public List<Forum> listAllForums() {
        List<Forum> forums = forumMapper.selectList(null);
        forums.forEach(this::setLogoUrl);
        return forums;
    }

    @Override
    public boolean addForum(Forum forum) {
        if(getForumByName(forum.getName()) != null){
            throw new ServiceException("同名板块已存在");
        }

        return forumMapper.insert(forum) > 0;
    }

    @Override
    public boolean updateForum(Forum forum) {
        Forum forum2 = getForumByName(forum.getName());
        if(forum2 != null && !Objects.equals(forum.getId(), forum2.getId())){
            throw new ServiceException("同名板块已存在");
        }

        return forumMapper.updateById(forum) > 0;
    }

    @Override
    public List<String> listCategories() {
        QueryWrapper<Forum> queryWrapper = new QueryWrapper<>();
        queryWrapper.groupBy("category");
        queryWrapper.select("category");
        List<Forum> forums = forumMapper.selectList(queryWrapper);
        return forums.stream().map(Forum::getCategory).collect(Collectors.toList());
    }

    @Override
    public boolean setForumLogo(Long forumId, MultipartFile file) {
        //删除原logo
        Forum originForum = forumMapper.selectById(forumId);
        if(originForum == null) return false;
        fileService.deleteFile(originForum.getLogoFileId());

        //保存图片
        FileIdentity logoFile = fileService.uploadImage(FORUM_LOGO_SOURCE, file, systemConfig.getMaxLogoImageSize(),false);

        Forum forum = new Forum();
        forum.setId(forumId);
        forum.setLogoFileId(logoFile.getId());
        return forumMapper.updateById(forum) > 0;
    }

    private Forum getForumByName(String forumName){
        Forum forumQuery = new Forum();
        forumQuery.setName(forumName);
        return forumMapper.selectOne(new QueryWrapper<>(forumQuery));
    }

    /**
     * 设置logo url字段
     */
    private void setLogoUrl(Forum forum){
        if(forum != null){
            forum.setLogoUrl(fileService.getFileUrl(forum.getLogoFileId()));
        }
    }
}
