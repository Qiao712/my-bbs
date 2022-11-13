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
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("isAuthenticated() and hasAuthority('post:add')")
    public Result<Void> addPost(@Validated(AddGroup.class) @RequestBody Post post){
        return Result.build(postService.addPost(post));
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasAuthority('post:get')")
    public Result<PostDto> getPost(@PathVariable("postId") Long postId){
        return Result.succeedNotNull(postService.getPost(postId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('post:list')")
    public Result<IPage<PostDto>> listPosts(@Validated PageQuery pageQuery, Long forumId, String authorUsername){
        return Result.succeed(postService.listPosts(pageQuery, forumId, authorUsername));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('post:search')")
    public Result<IPage<PostDto>> searchPosts(@Validated PageQuery pageQuery,
                                              @NotNull @NotBlank @Length(max = 30) String text,
                                              Long authorId, Long forumId){
        return Result.succeed(postService.searchPosts(pageQuery, text, authorId, forumId));
    }

    //TODO: bug
    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated() and postService.isAuthor(postId, currentUser.id) and hasAuthority('post:remove:mine')")
    public Result<Void> removeMyPost(@PathVariable("postId") Long postId, @AuthenticationPrincipal AuthUser currentUser){
        return Result.build(postService.removePost(postId));
    }

    //-----------------------------------------
    @GetMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated() and hasAuthority('post:like')")
    public Result<Void> likePost(@PathVariable("postId") Long postId){
        likeService.likePost(postId, true);
        return Result.succeed();
    }

    @GetMapping("/{postId}/undo-like")
    @PreAuthorize("isAuthenticated() and hasAuthority('post:like')")
    public Result<Void> undoLikePost(@PathVariable("postId") Long postId){
        likeService.likePost(postId, false);
        return Result.succeed();
    }
}
