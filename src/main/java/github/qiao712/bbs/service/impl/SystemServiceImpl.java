package github.qiao712.bbs.service.impl;

import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.domain.entity.Advertisement;
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

import java.util.Collections;
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
        //锁定图片文件(设置为非临时的)
        if(advertisement.getImageFileId() != null){
            fileService.setTempFlags(Collections.singletonList(advertisement.getImageFileId()), false);
        }

        return advertisementMapper.insert(advertisement) > 0;
    }

    @Override
    public List<Advertisement> listAdvertisement() {
        List<Advertisement> advertisements = advertisementMapper.selectList(null);

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

        //处理文件引用
        if(advertisement.getImageFileId() != null){
            fileService.setTempFlags(Collections.singletonList(oldAdvertisement.getId()), true);
            if(! fileService.setTempFlags(Collections.singletonList(advertisement.getImageFileId()), false)){
                throw new ServiceException("引用无效文件");
            }
        }

        return true;
    }
}
