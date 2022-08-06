package github.qiao712.bbs.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemProperties;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.exception.FileUploadException;
import github.qiao712.bbs.mapper.FileMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.util.FileUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AliOSSFileServiceImpl extends ServiceImpl<FileMapper, FileIdentity> implements FileService {
    @Autowired
    private OSS ossClient;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private SystemProperties systemProperties;

    @Override
    public FileIdentity uploadFile(String path, String fileType, InputStream inputStream, boolean isTemporary) {
        String filename = generatorFileName() + (fileType != null ? ("." + fileType) : "");
        String filepath = path + '/' + filename;

        try{
            PutObjectRequest putObjectRequest = new PutObjectRequest(systemProperties.getAliOSS().getBucketName(), filepath, inputStream);

            //设置权限--公共读
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            metadata.setObjectAcl(CannedAccessControlList.PublicRead);
            putObjectRequest.setMetadata(metadata);

            ossClient.putObject(putObjectRequest);
        } catch (OSSException | ClientException e) {
            throw new FileUploadException(e);
        }

        FileIdentity fileIdentity = new FileIdentity();
        fileIdentity.setFilepath(filepath);
        fileIdentity.setType(fileType);
        fileIdentity.setIsTemporary(isTemporary);
        fileIdentity.setUploaderId(SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null);  //文件上传者
        fileMapper.insert(fileIdentity);

        return fileIdentity;
    }

    @Override
    public FileIdentity uploadFile(String path, MultipartFile file, boolean isTemporary) {
        try(InputStream inputStream = file.getInputStream()){
            String fileType = FileUtil.getSuffix(file.getOriginalFilename());
            return uploadFile(path, fileType, inputStream, isTemporary);
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    public boolean setTempFlag(Long fileId, boolean isTemporary) {
        FileIdentity fileIdentity = new FileIdentity();
        fileIdentity.setId(fileId);
        fileIdentity.setIsTemporary(isTemporary);
        return fileMapper.updateById(fileIdentity) > 0;
    }


    @Override
    public String getFileUrl(Long fileId) {
        FileIdentity fileIdentity = fileMapper.selectById(fileId);

        if(fileIdentity != null){
            return getFileUrl(fileIdentity.getFilepath());
        }
        return null;
    }

    @Override
    public FileIdentity getFileIdentityByUrl(String url) {
        String filepath = getFilepathFromUrl(url);
        QueryWrapper<FileIdentity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("filepath", filepath);
        return fileMapper.selectOne(queryWrapper);
    }

    @Override
    public List<String> getBatchFileUrls(List<Long> fileIds) {
        List<FileIdentity> fileIdentities = fileMapper.selectBatchIds(fileIds);
        return fileIdentities.stream().map(FileIdentity::getFilepath).map(this::getFileUrl).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteFile(Long fileId){
        FileIdentity fileIdentity = fileMapper.selectById(fileId);
        if(fileIdentity != null){
            ossClient.deleteObject(systemProperties.getAliOSS().getBucketName(), fileIdentity.getFilepath());
            return fileMapper.deleteById(fileId) > 0;
        }
        return false;
    }

    @Override
    public boolean getFile(Long fileId, OutputStream outputStream){
        FileIdentity fileIdentity = fileMapper.selectById(fileId);
        if(fileIdentity != null){
            OSSObject ossObject = ossClient.getObject(systemProperties.getAliOSS().getBucketName(), fileIdentity.getFilepath());
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

    private String getFileUrl(String key){
        return "https://" + systemProperties.getAliOSS().getBucketName()
                + "." + systemProperties.getAliOSS().getEndpoint()
                + "/" + key;
    }

    public String getFilepathFromUrl(String url){
        int index = url.indexOf(systemProperties.getAliOSS().getEndpoint());
        if(index == -1) return null;
        return url.substring(index + systemProperties.getAliOSS().getEndpoint().length() + 1);
    }
}
