package qiao.qasys.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.StorageClass;
import qiao.qasys.common.ResultCode;
import qiao.qasys.config.SystemConfig;
import qiao.qasys.exception.ServiceException;
import qiao.qasys.service.FileService;
import qiao.qasys.util.FileUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
public class AliOSSFileServiceImpl implements FileService, InitializingBean {
    @Autowired
    private OSS ossClient;

    @Autowired
    private SystemConfig systemConfig;
    private String bucketName;
    private String endpoint;

    //初始化配置
    @Override
    public void afterPropertiesSet() throws Exception {
        this.bucketName = systemConfig.getAliOSS().getBucketName();
        this.endpoint = systemConfig.getAliOSS().getEndpoint();
    }

    @Override
    public String uploadFile(MultipartFile file, Long maxSize, Set<String> legalType) {
        //检查文件是否合法
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if(legalType != null && !legalType.contains(fileType)) throw new ServiceException(ResultCode.FAILURE, "文件类型非法");
        if(file.getSize() > maxSize) throw new ServiceException(ResultCode.UPLOAD_ERROR, "文件大小超出限制");

        try(InputStream inputStream = file.getInputStream()){
            return uploadFile(fileType, inputStream);
        } catch (IOException e) {
            throw new ServiceException(ResultCode.UPLOAD_ERROR);
        }
    }

    @Override
    public String uploadImage(MultipartFile file, Long maxSize) {
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if(! FileUtil.isPictureFile(fileType)) throw new ServiceException(ResultCode.UPLOAD_ERROR, "非图片文件");

        return uploadFile(file, maxSize, null);
    }

    private String uploadFile(String type, InputStream inputStream) {
        String filename = generatorFileName() + (type != null ? ("." + type) : "");
        String filepath = "qa/" + filename;

        try{
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filepath, inputStream);

            //设置权限--公共读
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            putObjectRequest.setMetadata(metadata);

            ossClient.putObject(putObjectRequest);
        } catch (OSSException | ClientException e) {
            throw new ServiceException(ResultCode.UPLOAD_ERROR);
        }

        return getUrl(filepath);
    }

    @Override
    public String getUrl(String key){
        return "https://" + bucketName
                + "." + endpoint
                + "/" + key;
    }

    private String generatorFileName(){
        return UUID.randomUUID().toString();
    }
}
