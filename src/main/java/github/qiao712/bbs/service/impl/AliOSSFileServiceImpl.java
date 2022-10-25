package github.qiao712.bbs.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.exception.FileUploadException;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.FileMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.util.FileUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.toJSONString;

@Service
public class AliOSSFileServiceImpl extends ServiceImpl<FileMapper, FileIdentity> implements FileService, InitializingBean {
    @Autowired
    private OSS ossClient;
    @Autowired
    private FileMapper fileMapper;

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
    public FileIdentity uploadFile(String source, MultipartFile file, Long maxSize, Set<String> legalType, boolean isTemporary) {
        //检查文件是否合法
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if(legalType != null && !legalType.contains(fileType)) throw new ServiceException("文件类型非法");
        if(file.getSize() > maxSize) throw new ServiceException("文件大小超出限制");

        try(InputStream inputStream = file.getInputStream()){
            return uploadFile(source, fileType, inputStream, isTemporary);
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    public FileIdentity uploadImage(String source, MultipartFile file, Long maxSize, boolean isTemporary) {
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if(! FileUtil.isPictureFile(fileType)) throw new ServiceException("非图片文件");

        return uploadFile(source, file, maxSize, null, isTemporary);
    }

    private FileIdentity uploadFile(String source, String type, InputStream inputStream, boolean isTemporary) {
        String filename = generatorFileName() + (type != null ? ("." + type) : "");
        String filepath = source + '/' + filename;

        try{
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filepath, inputStream);

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
        fileIdentity.setSource(source);
        fileIdentity.setType(type);
        fileIdentity.setIsTemporary(isTemporary);
        fileIdentity.setUploaderId(SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null);  //文件上传者
        fileMapper.insert(fileIdentity);

        return fileIdentity;
    }

    @Override
    public boolean setTempFlags(List<Long> fileIds, boolean isTemporary) {
        return fileMapper.updateTempFlag(fileIds, isTemporary) > 0;
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
    public FileIdentity getFileIdentity(Long fileId) {
        return fileMapper.selectById(fileId);
    }

    @Override
    @Transactional
    public boolean deleteFile(Long fileId){
        FileIdentity fileIdentity = fileMapper.selectById(fileId);
        if(fileIdentity != null){
            ossClient.deleteObject(bucketName, fileIdentity.getFilepath());
            return fileMapper.deleteById(fileId) > 0;
        }
        return false;
    }

    @Override
    public void clearTemporaryFile() {
        QueryWrapper<FileIdentity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_temporary", true);
        queryWrapper.le("is_temporary", LocalDateTime.now().minusSeconds(systemConfig.getMinTempFileLife()));
        Page<FileIdentity> page = new Page<>(1, 1000);  //阿里云OSS批量删除，一次最多1000个

        while(true){
            fileMapper.selectPage(page, queryWrapper);
            List<FileIdentity> files = page.getRecords();
            if(files.isEmpty()) break;
            List<String> keys = files.stream().map(FileIdentity::getFilepath).collect(Collectors.toList());

            //从OSS中批量删除
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
            deleteObjectsRequest.setKeys(keys);
            deleteObjectsRequest.setQuiet(true);    //简单模式，返回删除失败的文件列表
            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
            List<String> notDeletedObjects = deleteObjectsResult.getDeletedObjects();   //删除失败的文件列表

            //成功删除的文件的id
            List<Long> ids;
            if(notDeletedObjects.isEmpty()){
                ids = files.stream().map(FileIdentity::getId).collect(Collectors.toList());
            }else{
                Set<String> notDeletedObjectsSet = new HashSet<>(notDeletedObjects);
                ids = new ArrayList<>(files.size() - notDeletedObjectsSet.size());
                for (FileIdentity file : files) {
                    if(!notDeletedObjectsSet.contains(file.getFilepath())) ids.add(file.getId());
                }
            }

            fileMapper.deleteBatchIds(ids);

            //下一页
            page.setCurrent(page.getCurrent() + 1);
        }
    }

    @Override
    public boolean getFile(Long fileId, OutputStream outputStream){
        FileIdentity fileIdentity = fileMapper.selectById(fileId);
        if(fileIdentity != null){
            OSSObject ossObject = ossClient.getObject(bucketName, fileIdentity.getFilepath());
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
        return "https://" + bucketName
                + "." + endpoint
                + "/" + key;
    }

    private String getFilepathFromUrl(String url){
        int index = url.indexOf(endpoint);
        if(index == -1) return null;
        return url.substring(index + endpoint.length() + 1);
    }
}
