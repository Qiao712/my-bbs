package github.qiao712.bbs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.domain.entity.Advertisement;
import github.qiao712.bbs.domain.base.ResultCode;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.AdvertisementMapper;
import github.qiao712.bbs.mapper.CommentMapper;
import github.qiao712.bbs.mapper.PostMapper;
import github.qiao712.bbs.mapper.UserMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class SystemServiceImpl implements SystemService {
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private AdvertisementMapper advertisementMapper;

    @Override
    public Statistic getStatistic() {
        Statistic statistic = new Statistic();
        statistic.setPostCount(postMapper.selectCount(null));
        statistic.setUserCount(userMapper.selectCount(null));
        statistic.setCommentCount(commentMapper.selectCount(null));
        return statistic;
    }


    @Override
    @Transactional
    public boolean addAdvertisement(Advertisement advertisement) {
        referenceImage(advertisement.getImageFileId());
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
        Advertisement advertisement = advertisementMapper.selectById(advertisementId);

        //释放图片
        fileService.increaseReferenceCount(advertisement.getImageFileId(), -1);

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

        //释放原图片
        fileService.increaseReferenceCount(oldAdvertisement.getImageFileId(), -1);

        //引用新图片
        if(advertisement.getImageFileId() != null)
            referenceImage(advertisement.getImageFileId());

        return true;
    }

    /**
     * 引用图片文件
     */
    private void referenceImage(Long imageFileId){
        if(imageFileId != null){
            //限制其只能引用通过广告图片接口上传的图片
            String source = fileService.getFileIdentity(imageFileId).getSource();
            if(FileService.ADVERTISEMENT_IMAGE_FILE.equals(source)){
                if(!fileService.increaseReferenceCount(imageFileId, 1)){
                    throw new ServiceException(ResultCode.INVALID_PARAM, "图片文件无效");
                }
            }else{
                throw new ServiceException(ResultCode.FILE_ERROR, "图片文件非法");
            }
        }else{
            throw new ServiceException(ResultCode.INVALID_PARAM, "未指定图片");
        }
    }
}
