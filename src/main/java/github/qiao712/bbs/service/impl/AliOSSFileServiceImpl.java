package github.qiao712.bbs.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemProperties;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.mapper.FileMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

@Service
public class AliOSSFileServiceImpl extends ServiceImpl<FileMapper, FileIdentity> implements FileService {
    @Autowired
    private OSS ossClient;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public Long saveFile(String dir, String fileType, InputStream inputStream) {
        String filename = generatorFileName() + (fileType != null ? ("." + fileType) : "");
        String path = dir + '/' + filename;

        try{
            PutObjectRequest putObjectRequest = new PutObjectRequest(systemProperties.getAliOSS().getBucketName(), path, inputStream);

            //设置权限--公共读
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            putObjectRequest.setMetadata(metadata);

            ossClient.putObject(putObjectRequest);
        } catch (OSSException | ClientException e) {
            throw new RuntimeException("文件上传失败", e);
        }

        FileIdentity fileIdentity = new FileIdentity();
        fileIdentity.setPath(path);
        fileIdentity.setType(fileType);
        fileIdentity.setRefCount(1);
        fileMapper.insert(fileIdentity);

        return fileIdentity.getId();
    }

    @Override
    public Long saveFile(String dir, MultipartFile file) {
        try(InputStream inputStream = file.getInputStream()){
            String fileType = FileUtils.getPosix(file.getOriginalFilename());
            return saveFile(dir, fileType, inputStream);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @Override
    public String getFileUrl(Long id) {
        FileIdentity fileIdentity = fileMapper.selectById(id);

        if(fileIdentity != null){
            String url = "https://" + systemProperties.getAliOSS().getBucketName()
                         + "." + systemProperties.getAliOSS().getEndpoint()
                         + "/" + fileIdentity.getPath();
            return url;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean deleteFile(Long id){
        FileIdentity fileIdentity = fileMapper.selectById(id);
        if(fileIdentity != null){
            ossClient.deleteObject(systemProperties.getAliOSS().getBucketName(), fileIdentity.getPath());
            return fileMapper.deleteById(id) > 0;
        }
        return false;
    }

    @Override
    public boolean getFile(Long id, OutputStream outputStream){
        FileIdentity fileIdentity = fileMapper.selectById(id);
        if(fileIdentity != null){
            OSSObject ossObject = ossClient.getObject(systemProperties.getAliOSS().getBucketName(), fileIdentity.getPath());
            InputStream inputStream = ossObject.getObjectContent();
            try {
                FileCopyUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    private String generatorFileName(){
        return UUID.randomUUID().toString();
    }
}
