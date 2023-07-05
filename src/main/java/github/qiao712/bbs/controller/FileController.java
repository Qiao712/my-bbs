package github.qiao712.bbs.controller;

import github.qiao712.bbs.config.SystemConfig;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileService fileService;
    @Autowired
    private SystemConfig systemConfig;

    /**
     * 上传用户头像图片
     */
    @PreAuthorize("isAuthenticated() and hasAuthority('file:upload')")
    @PostMapping("/")
    public Result<String> uploadUserAvatarImage(@RequestPart("file") MultipartFile file){
        return Result.succeedNotNull(fileService.uploadImage(file, systemConfig.getMaxImageSize()));
    }
}
