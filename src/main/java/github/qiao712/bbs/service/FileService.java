package github.qiao712.bbs.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface FileService{
    /**
     * 上传文件
     * @param file 请求上传的文件
     * @param maxSize 所允许的最大文件大小
     * @param legalType 合法的文件类型集合
     * @return 返回文件路径
     */
    String uploadFile(MultipartFile file, Long maxSize, Set<String> legalType);

    String uploadImage(MultipartFile file, Long maxSize);

    String getUrl(String path);
}
