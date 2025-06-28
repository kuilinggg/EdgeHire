package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    // 可根据需要添加自定义查询方法
}
