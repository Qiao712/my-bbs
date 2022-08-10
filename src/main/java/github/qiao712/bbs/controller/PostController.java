package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.FileIdentity;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.service.PostService;
import github.qiao712.bbs.service.impl.PostServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @PostMapping
    public Result<Void> addPost(@Validated(AddGroup.class) @RequestBody Post post){
        return Result.build(postService.addPost(post));
    }

    @PostMapping("/pictures")
    public Result<String> uploadPicture(@RequestPart("picture") MultipartFile pictureFile){
        return Result.succeed("图片上传成功", postService.uploadPicture(pictureFile));
    }

    @GetMapping("/{postId}")
    public Result<PostDto> getPost(@PathVariable("postId") Long postId){
        return Result.succeedNotNull(postService.getPost(postId));
    }

    @GetMapping
    public Result<IPage<PostDto>> listPosts(PageQuery pageQuery, Long forumId){
        return Result.succeed(postService.listPosts(pageQuery, forumId));
    }
}
