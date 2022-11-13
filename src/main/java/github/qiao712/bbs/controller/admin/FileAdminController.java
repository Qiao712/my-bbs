package github.qiao712.bbs.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.FileInfoDto;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/files")
public class FileAdminController {
    @Autowired
    private FileService fileService;

    @PreAuthorize("hasAuthority('admin:file:list')")
    @GetMapping
    public Result<IPage<FileInfoDto>> listFiles(PageQuery pageQuery, FileIdentity query){
        return Result.succeed(fileService.listFileIdentities(pageQuery, query));
    }

    @PreAuthorize("hasAuthority('admin:file:delete')")
    @DeleteMapping("/{fileId}")
    public Result<Void> removeFile(@PathVariable("fileId") Long fileId){
        return Result.build(fileService.deleteFile(fileId));
    }
}
