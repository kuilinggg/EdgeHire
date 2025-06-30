package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    List<Post> findByUserId(Integer userId);

    // 根据userId获取投递记录及其关联信息
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.seekerInfo LEFT JOIN FETCH p.resume WHERE p.userId = :userId")
    List<Post> findByUserIdWithDetails(@Param("userId") Integer userId);

    // 获取所有投递记录及其关联信息
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.seekerInfo LEFT JOIN FETCH p.resume")
    List<Post> findAllWithDetails();

    // 根据条件筛选投递记录
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user LEFT JOIN FETCH p.seekerInfo LEFT JOIN FETCH p.resume " +
           "WHERE (:keyword IS NULL OR p.user.username LIKE %:keyword% OR p.seekerInfo.favor LIKE %:keyword%) " +
           "AND (:education IS NULL OR p.seekerInfo.education = :education) " +
           "AND (:favor IS NULL OR p.seekerInfo.favor LIKE %:favor%)")
    List<Post> findByFilter(@Param("keyword") String keyword, 
                           @Param("education") Integer education, 
                           @Param("favor") String favor);
}
