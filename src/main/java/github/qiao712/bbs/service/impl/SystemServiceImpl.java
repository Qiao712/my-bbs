package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.domain.entity.Advertisement;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AdvertisementMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.QuestionMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Component
public class SystemServiceImpl implements SystemService {
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private AdvertisementMapper advertisementMapper;
    @Autowired
    private SystemConfig systemConfig;

    private final static String ADVERTISEMENT_IMAGE_SOURCE = "ad-image";

    @Override
    public Statistic getStatistic() {
        Statistic statistic = new Statistic();
        statistic.setPostCount(questionMapper.selectCount(null));
        statistic.setUserCount(userMapper.selectCount(null));
        statistic.setCommentCount(commentMapper.selectCount(null));
        return statistic;
    }

    @Override
    public Long uploadAdvertisementImage(MultipartFile imageFile) {
        FileIdentity fileIdentity = fileService.uploadImage(ADVERTISEMENT_IMAGE_SOURCE, imageFile, systemConfig.getMaxAdvertisementImageSize(), true);
        return fileIdentity != null ? fileIdentity.getId() : null;
    }

    @Override
    @Transactional
    public boolean addAdvertisement(Advertisement advertisement) {
        useImage(advertisement.getImageFileId());
        return advertisementMapper.insert(advertisement) > 0;
    }

    @Override
    public List<Advertisement> listAdvertisements() {
        LambdaQueryWrapper<Advertisement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Advertisement::getSequence);
        List<Advertisement> advertisements = advertisementMapper.selectList(queryWrapper);

        //获取图片url
        for (Advertisement advertisement : advertisements) {
            String imageUrl = fileService.getFileUrl(advertisement.getImageFileId());
            advertisement.setImageUrl(imageUrl);
        }

        return advertisements;
    }

    @Override
    @Transactional
    public boolean removeAdvertisement(Long advertisementId) {
        advertisementMapper.selectById(advertisementId);

        //释放图片
        fileService.setTempFlags(Collections.singletonList(advertisementId), true);

        return advertisementMapper.deleteById(advertisementId) > 0;
    }

    @Override
    @Transactional
    public boolean updateAdvertisement(Advertisement advertisement) {
        Advertisement oldAdvertisement = advertisementMapper.selectById(advertisement.getId());
        if(oldAdvertisement == null) return false;

        if(advertisementMapper.updateById(advertisement) == 0){
            return false;
        }

        if(advertisement.getImageFileId() != null)
            useImage(advertisement.getImageFileId());

        return true;
    }

    /**
     * 使用并锁定图片
     */
    private void useImage(Long imageFileId){
        if(imageFileId != null){
            //限制其只能引用通过广告图片接口上传的图片
            String source = fileService.getFileIdentity(imageFileId).getSource();
            if(ADVERTISEMENT_IMAGE_SOURCE.equals(source)){
                if(!fileService.setTempFlags(Collections.singletonList(imageFileId), false)){
                    throw new ServiceException("图片文件无效");
                }
            }else{
                throw new ServiceException("图片文件非法");
            }
        }else{
            throw new ServiceException("未指定图片");
        }
    }
}
