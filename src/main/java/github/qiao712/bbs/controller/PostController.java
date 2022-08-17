package github.qiao712.bbs.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import github.qiao712.bbs.domain.AddGroup;
import github.qiao712.bbs.domain.base.PageQuery;
import github.qiao712.bbs.domain.base.Result;
import github.qiao712.bbs.domain.dto.AuthUser;
import github.qiao712.bbs.domain.dto.PostDto;
import github.qiao712.bbs.domain.entity.Post;
import github.qiao712.bbs.service.LikeService;
import github.qiao712.bbs.service.PostService;
import github.qiao712.bbs.service.SearchService;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;
    @Autowired
    private LikeService likeService;

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
    public Result<IPage<PostDto>> listPosts(@Validated PageQuery pageQuery, Long forumId, Long authorId){
        return Result.succeed(postService.listPosts(pageQuery, forumId, authorId));
    }

    @GetMapping("/search")
    public Result<IPage<PostDto>> searchPosts(@Validated PageQuery pageQuery,
                                              @NotNull @NotBlank @Length(max = 30) String text,
                                              Long authorId, Long forumId){
        return Result.succeed(postService.searchPosts(pageQuery, text, authorId, forumId));
    }

    @DeleteMapping("/{postId}")
    public Result<Void> removePost(@PathVariable("postId") Long postId, @AuthenticationPrincipal AuthUser currentUser){
        if(currentUser.getRole().equals("ROLE_ADMIN") || postService.isAuthor(postId, currentUser.getId())){
            return Result.build(postService.removePost(postId));
        }else{
            throw new AccessDeniedException("无权删除该贴子");
        }
    }

    //-----------------------------------------
    @GetMapping("/{postId}/like")
    public Result<Void> likePost(@PathVariable("postId") Long postId){
        return Result.build(likeService.likePost(postId));
    }

    @GetMapping("/{postId}/undo-like")
    public Result<Void> undoLikePost(@PathVariable("postId") Long postId){
        return Result.build(likeService.undoLikePost(postId));
    }
}
