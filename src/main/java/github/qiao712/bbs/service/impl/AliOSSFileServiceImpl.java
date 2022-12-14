package github.qiao712.bbs.service.impl;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.internal.OSSHeaders;
import com.aliyun.oss.model.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.dto.FileInfoDto;
import github.qiao712.bbs.domain.dto.FileURL;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.domain.entity.User;
import github.qiao712.bbs.exception.FileUploadException;
import github.qiao712.bbs.exception.ServiceException;
import github.qiao712.bbs.mapper.FileMapper;
import github.qiao712.bbs.service.FileService;
import github.qiao712.bbs.service.UserService;
import github.qiao712.bbs.util.FileUtil;
import github.qiao712.bbs.util.PageUtil;
import github.qiao712.bbs.util.SecurityUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alibaba.fastjson.JSON.toJSONString;

@Service
public class AliOSSFileServiceImpl extends ServiceImpl<FileMapper, FileIdentity> implements FileService, InitializingBean {
    @Autowired
    private OSS ossClient;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfig systemConfig;
    private String bucketName;
    private String endpoint;

    //???????????????
    @Override
    public void afterPropertiesSet() throws Exception {
        this.bucketName = systemConfig.getAliOSS().getBucketName();
        this.endpoint = systemConfig.getAliOSS().getEndpoint();
    }

    @Override
    public FileURL uploadFile(String source, MultipartFile file, Long maxSize, Set<String> legalType) {
        //????????????????????????
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if(legalType != null && !legalType.contains(fileType)) throw new ServiceException("??????????????????");
        if(file.getSize() > maxSize) throw new ServiceException("????????????????????????");

        try(InputStream inputStream = file.getInputStream()){
            return uploadFile(source, fileType, inputStream);
        } catch (IOException e) {
            throw new FileUploadException(e);
        }
    }

    @Override
    public FileURL uploadImage(String source, MultipartFile file, Long maxSize) {
        String fileType = FileUtil.getSuffix(file.getOriginalFilename());
        if(! FileUtil.isPictureFile(fileType)) throw new ServiceException("???????????????");

        return uploadFile(source, file, maxSize, null);
    }

    private FileURL uploadFile(String source, String type, InputStream inputStream) {
        String filename = generatorFileName() + (type != null ? ("." + type) : "");
        String filepath = source + '/' + filename;

        try{
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filepath, inputStream);

            //????????????--?????????
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
        fileIdentity.setRefCount(0);
        fileIdentity.setUploaderId(SecurityUtil.isAuthenticated() ? SecurityUtil.getCurrentUser().getId() : null);  //???????????????
        fileMapper.insert(fileIdentity);

        //????????????Id ??? Url
        FileURL fileURL = new FileURL();
        fileURL.setId(fileIdentity.getId());
        fileURL.setUrl(getFileUrl(fileIdentity.getFilepath()));
        return fileURL;
    }

    @Override
    public boolean increaseReferenceCount(List<Long> fileIds, int delta) {
        if(fileIds.isEmpty()) return false;
        return fileMapper.increaseRefCount(fileIds, delta) > 0;
    }

    @Override
    public boolean increaseReferenceCount(Long fileId, int delta) {
        return increaseReferenceCount(Collections.singletonList(fileId), delta);
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
    public IPage<FileInfoDto> listFileIdentities(PageQuery pageQuery, FileIdentity query) {
        QueryWrapper<FileIdentity> queryWrapper = new QueryWrapper<>(query);
        IPage<FileIdentity> filePage = fileMapper.selectPage(pageQuery.getIPage(), queryWrapper);
        List<FileIdentity> fileIdentities = filePage.getRecords();

        if(fileIdentities.isEmpty()){
            return PageUtil.replaceRecords(filePage, Collections.emptyList());
        }

        //???????????????
        Set<Long> uploaderIds = fileIdentities.stream().map(FileIdentity::getUploaderId).collect(Collectors.toSet());
        LambdaQueryWrapper<User> userQueryWrapper = new LambdaQueryWrapper<>();
        userQueryWrapper.select(User::getUsername, User::getId);
        userQueryWrapper.in(User::getId, uploaderIds);
        List<User> users = userService.list(userQueryWrapper);
        Map<Long, String> usernameMap = users.stream().collect(Collectors.toMap(User::getId, User::getUsername));

        List<FileInfoDto> fileInfoDtos = new ArrayList<>(fileIdentities.size());
        for (FileIdentity fileIdentity : fileIdentities) {
            FileInfoDto fileInfoDto = new FileInfoDto();
            BeanUtils.copyProperties(fileIdentity, fileInfoDto);
            fileInfoDto.setUploaderUsername(usernameMap.get(fileIdentity.getUploaderId()));
            fileInfoDto.setUrl(getFileUrl(fileIdentity.getFilepath()));
            fileInfoDtos.add(fileInfoDto);
        }

        return PageUtil.replaceRecords(filePage, fileInfoDtos);
    }

    @Override
    public void clearIdleFile() {
        QueryWrapper<FileIdentity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ref_count", 0);
        queryWrapper.le("update_time", LocalDateTime.now().minusSeconds(systemConfig.getMinTempFileLife()));
        Page<FileIdentity> page = new Page<>(1, 1000);  //?????????OSS???????????????????????????1000???

        while(true){
            fileMapper.selectPage(page, queryWrapper);
            List<FileIdentity> files = page.getRecords();
            if(files.isEmpty()) break;
            List<String> keys = files.stream().map(FileIdentity::getFilepath).collect(Collectors.toList());

            //???OSS???????????????
            DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName);
            deleteObjectsRequest.setKeys(keys);
            deleteObjectsRequest.setQuiet(true);    //????????????????????????????????????????????????
            DeleteObjectsResult deleteObjectsResult = ossClient.deleteObjects(deleteObjectsRequest);
            List<String> notDeletedObjects = deleteObjectsResult.getDeletedObjects();   //???????????????????????????

            //??????????????????????????????id
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

            //?????????????????????
            fileMapper.deleteBatchIds(ids);

            //?????????
            page.setCurrent(page.getCurrent() + 1);
        }
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
