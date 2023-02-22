package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.CacheConstant;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.domain.entity.Forum;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.ForumMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.ForumService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ForumServiceImpl extends ServiceImpl<ForumMapper, Forum> implements ForumService {
    @Autowired
    private ForumMapper forumMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private StringRedisTemplate redisTemplate;

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
            throw new ServiceException(ResultCode.INVALID_PARAM, "同名板块已存在");
        }

        return forumMapper.insert(forum) > 0;
    }

    @Override
    public boolean updateForum(Forum forum) {
        Forum forum2 = getForumByName(forum.getName());
        if(forum2 != null && !Objects.equals(forum.getId(), forum2.getId())){
            throw new ServiceException(ResultCode.INVALID_PARAM, "同名板块已存在");
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
    public boolean setForumLogo(Long forumId, Long fileId) {
        //限制图片来源
        FileIdentity fileIdentity = fileService.getFileIdentity(fileId);
        if(fileIdentity != null && !Objects.equals(fileIdentity.getSource(), FileService.FORUM_LOGO_IMAGE_FILE)){
            throw new ServiceException(ResultCode.INVALID_PARAM, "图片非法");
        }

        //释放原有图片
        Forum originForum = forumMapper.selectById(forumId);
        if(originForum == null) return false;
        fileService.increaseReferenceCount(originForum.getLogoFileId(), -1);

        //引用图片
        fileService.increaseReferenceCount(fileId, 1);

        Forum forum = new Forum();
        forum.setId(forumId);
        forum.setLogoFileId(fileId);
        return forumMapper.updateById(forum) > 0;
    }

    @Override
    public Long getPostCount(Long forumId) {
        String key = CacheConstant.POST_COUNT_KEY_PREFIX + forumId;
        String value = redisTemplate.opsForValue().get(key);
        if(value != null){
            return Long.parseLong(value);
        }else{
            Long count = forumMapper.selectPostCount(forumId);
            redisTemplate.opsForValue().set(key, count.toString(), CacheConstant.POST_CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            return count;
        }
    }

    @Override
    public void increasePostCount(Long forumId, Long delta) {
        forumMapper.increasePostCount(forumId, delta);
        redisTemplate.delete(CacheConstant.POST_COUNT_KEY_PREFIX+forumId);
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
