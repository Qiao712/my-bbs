package github.qiao712.bbs.controller;

import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.FileURL;
import github.qiao712.bbs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private SystemConfig systemConfig;

    @GetMapping("/{fileId}/url")
    public Result<String> getFileUrl(@PathVariable("fileId") Long fileId){
        return Result.succeed("", fileService.getFileUrl(fileId));
    }

    /**
     * 上传用户头像图片
     */
    @PreAuthorize("isAuthenticated() and hasAuthority('file:user-avatar:upload')")
    @PostMapping("/user-avatars")
    public Result<FileURL> uploadUserAvatarImage(@RequestPart("file") MultipartFile file){
        return Result.succeedNotNull(fileService.uploadImage(FileService.USER_AVATAR_IMAGE_FILE, file, systemConfig.getMaxAvatarSize()));
    }

    /**
     * 上传论坛logo图片
     */
    @PreAuthorize("isAuthenticated() and hasAuthority('file:forum-logo:upload')")
    @PostMapping("/forum-logos")
    public Result<FileURL> uploadForumLogoImage(@RequestPart("file") MultipartFile file){
        return Result.succeedNotNull(fileService.uploadImage(FileService.FORUM_LOGO_IMAGE_FILE, file, systemConfig.getMaxLogoImageSize()));
    }

    /**
     * 上传贴子、评论中插入的图片
     */
    @PreAuthorize("isAuthenticated() and hasAuthority('file:post-image:upload')")
    @PostMapping("/post-images")
    public Result<FileURL> uploadPostImage(@RequestPart("file") MultipartFile file){
        return Result.succeedNotNull(fileService.uploadImage(FileService.POST_IMAGE_FILE, file, systemConfig.getMaxPostImageSize()));
    }

    /**
     * 上传广告图片
     */
    @PreAuthorize("isAuthenticated() and hasAuthority('file:advertisement:upload')")
    @PostMapping("/advertisement-images")
    public Result<FileURL> uploadAdvertisementImage(@RequestPart("file") MultipartFile file){
        return Result.succeedNotNull(fileService.uploadImage(FileService.ADVERTISEMENT_IMAGE_FILE, file, systemConfig.getMaxAdvertisementImageSize()));
    }
}
