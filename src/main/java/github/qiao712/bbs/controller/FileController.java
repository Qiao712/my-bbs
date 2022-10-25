package github.qiao712.bbs.controller;

import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
