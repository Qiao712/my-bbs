package github.qiao712.bbs.controller;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping("/{fileId}/url")
    public Result<String> getFileUrl(@PathVariable("fileId") Long fileId){
        return Result.succeed("", fileService.getFileUrl(fileId));
    }
}
