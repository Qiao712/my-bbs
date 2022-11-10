package github.qiao712.bbs.service;

import github.qiao712.bbs.domain.dto.Statistic;
import github.qiao712.bbs.domain.entity.Advertisement;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SystemService {
    /**
     * 获取整个系统的统计信息
     * 贴子数量、评论数量、用户数量
     */
    Statistic getStatistic();

    //首页广告--------------------------------------

    boolean addAdvertisement(Advertisement advertisement);

    List<Advertisement> listAdvertisements();

    boolean removeAdvertisement(Long advertisingId);

    boolean updateAdvertisement(Advertisement advertisement);
}
