package com.se.EdgeHire.Controller;

import com.se.EdgeHire.Entity.Post;
import com.se.EdgeHire.Service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/user/{userId}")
    public List<Post> getPostsByUserId(@PathVariable Integer userId) {
        return postService.getPostsByUserId(userId);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Optional<Post> getPostById(@PathVariable Integer id) {
        return postService.getPostById(id);
    }

    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return postService.createPost(post);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Integer id) {
        postService.deletePost(id);
    }

    // 获取所有投递记录及其关联信息
    @GetMapping("/with-details")
    public List<Post> getPostsWithDetails() {
        return postService.getAllPostsWithDetails();
    }

    // 根据条件筛选投递记录
    @GetMapping("/filter")
    public List<Post> getPostsByFilter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer education,
            @RequestParam(required = false) Integer membership) {
        return postService.getPostsByFilter(keyword, education, membership);
    }

    // 根据userId获取投递记录及其关联信息
    @GetMapping("/user/{userId}/with-details")
    public List<Post> getPostsByUserIdWithDetails(@PathVariable Integer userId) {
        return postService.getPostsByUserIdWithDetails(userId);
    }
}
