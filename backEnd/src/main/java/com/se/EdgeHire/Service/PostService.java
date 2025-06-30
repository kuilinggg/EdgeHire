package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.Post;
import com.se.EdgeHire.Repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Integer id) {
        return postRepository.findById(id);
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Integer id) {
        postRepository.deleteById(id);
    }

    public List<Post> getPostsByUserId(Integer userId) {
        return postRepository.findByUserId(userId);
    }

    // 获取所有投递记录及其关联信息
    public List<Post> getAllPostsWithDetails() {
        return postRepository.findAllWithDetails();
    }

    // 根据条件筛选投递记录
    public List<Post> getPostsByFilter(String keyword, Integer education, String favor) {
        return postRepository.findByFilter(keyword, education, favor);
    }

    // 根据userId获取投递记录及其关联信息
    public List<Post> getPostsByUserIdWithDetails(Integer userId) {
        return postRepository.findByUserIdWithDetails(userId);
    }
}
