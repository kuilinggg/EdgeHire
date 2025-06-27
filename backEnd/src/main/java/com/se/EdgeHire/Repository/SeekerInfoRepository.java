package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.SeekerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeekerInfoRepository extends JpaRepository<SeekerInfo, Integer> {
    Optional<SeekerInfo> findByUserId(Integer userId);
    boolean existsByUserId(Integer userId);
    
    // 根据学历查找求职者
    List<SeekerInfo> findByEducation(Integer education);
    
    // 根据理想岗位模糊查找
    List<SeekerInfo> findByFavorContaining(String favor);
    
    // 根据会员等级查找
    List<SeekerInfo> findByMembership(Integer membership);
    
    // 复合查询：根据学历和理想岗位查找
    @Query("SELECT s FROM SeekerInfo s WHERE s.education >= :minEducation AND s.favor LIKE %:favor%")
    List<SeekerInfo> findByEducationAndFavor(@Param("minEducation") Integer minEducation, @Param("favor") String favor);
}
