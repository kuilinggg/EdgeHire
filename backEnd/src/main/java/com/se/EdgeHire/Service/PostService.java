package com.se.EdgeHire.Service;

import com.se.EdgeHire.DTO.PostDTO;
import com.se.EdgeHire.Entity.Post;
import com.se.EdgeHire.Repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
    /**
     * 根据条件筛选投递记录（带 Redis 缓存）
     * 
     * 缓存策略说明：
     * 1. 缓存键（Key）：根据查询参数生成唯一标识，格式为 "posts:filter:关键词:学历:会员等级"
     * 2. 缓存命中：如果 Redis 中存在该键，直接返回缓存数据，避免数据库查询
     * 3. 缓存未命中：查询数据库，将结果转换为 DTO，存入 Redis（设置 5 分钟过期时间）
     * 4. 数据一致性：通过 TTL（过期时间）保证数据最终一致性，最多延迟 5 分钟
     * 
     * 为什么设置 5 分钟过期？
     * - 简历搜索场景对实时性要求不是特别高
     * - 5 分钟内重复搜索可以直接命中缓存，提升性能
     * - 5 分钟后自动失效，保证数据不会太陈旧
     * 
     * @param keyword 搜索关键词（可选）
     * @param education 学历要求（可选）
     * @param membership 会员等级（可选）
     * @return 符合条件的投递记录 DTO 列表
     */
    public List<PostDTO> getPostsByFilter(String keyword, Integer education, Integer membership) {
        // 第一步：生成缓存键
        // 使用冒号分隔，便于在 Redis 中查看和管理
        // 对 null 值使用 "null" 字符串，确保不同参数组合产生不同的键
        String cacheKey = String.format("posts:filter:%s:%s:%s", 
            keyword != null ? keyword : "null",
            education != null ? education : "null",
            membership != null ? membership : "null"
        );
        
        // 第二步：尝试从 Redis 获取缓存数据
        try {
            // opsForValue() 是操作 String 类型数据的接口
            Object cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                // 缓存命中！直接返回
                // 由于我们配置了 Jackson 序列化器，Redis 会自动将 JSON 反序列化为对象
                System.out.println("✅ 缓存命中: " + cacheKey);
                @SuppressWarnings("unchecked")  // 这个类型转换是安全的，因为我们存入时就是 List<PostDTO>
                List<PostDTO> result = (List<PostDTO>) cachedData;
                return result;
            }
        } catch (Exception e) {
            // Redis 出现异常时，不应该影响业务逻辑
            // 记录日志后继续查询数据库
            System.err.println("❌ Redis 查询异常: " + e.getMessage());
        }
        
        // 第三步：缓存未命中，查询数据库
        System.out.println("⚠️ 缓存未命中，查询数据库: " + cacheKey);
        List<Post> posts = postRepository.findByFilter(keyword, education, membership);
        
        // 第四步：将 JPA 实体转换为 DTO
        // 使用 Stream API 进行批量转换
        List<PostDTO> dtoList = posts.stream()
            .map(this::convertToDTO)  // 对每个 Post 调用 convertToDTO 方法
            .collect(Collectors.toList());  // 收集结果到 List
        
        // 第五步：将结果存入 Redis
        try {
            // set(key, value, timeout, timeUnit)
            // 5: 过期时间值
            // TimeUnit.MINUTES: 时间单位为分钟
            redisTemplate.opsForValue().set(cacheKey, dtoList, 5, TimeUnit.MINUTES);
            System.out.println("💾 数据已缓存: " + cacheKey + "，5 分钟后过期");
        } catch (Exception e) {
            // Redis 写入失败也不影响业务逻辑
            // 只是下次查询无法命中缓存而已
            System.err.println("❌ Redis 写入异常: " + e.getMessage());
        }
        
        // 第六步：返回结果
        return dtoList;
    }

    // 根据userId获取投递记录及其关联信息
    public List<Post> getPostsByUserIdWithDetails(Integer userId) {
        return postRepository.findByUserIdWithDetails(userId);
    }

    // 根据简历id批量删除post
    public void deletePostsByResumeId(Integer resumeId) {
        postRepository.deleteByResumeId(resumeId);
    }
    
    /**
     * 将 JPA 实体 Post 转换为 DTO
     * 
     * 为什么需要这个方法？
     * 1. JPA 实体包含懒加载关联，离开 Session 后无法访问
     * 2. 直接缓存实体会导致序列化问题（循环引用、代理对象等）
     * 3. DTO 是纯 POJO，可以安全地序列化到 Redis
     * 
     * 设计思路：
     * 保持嵌套结构与前端期望一致，这样前端代码无需修改
     * 
     * @param post JPA 实体对象
     * @return PostDTO 数据传输对象
     */
    private PostDTO convertToDTO(Post post) {
        PostDTO dto = new PostDTO();
        
        // 复制 Post 基本信息
        dto.setId(post.getId());
        dto.setUserId(post.getUserId());
        dto.setSeekerInfoId(post.getSeekerInfoId());
        dto.setResumeId(post.getResumeId());
        
        // 转换 User 及其 Info
        if (post.getUser() != null) {
            PostDTO.UserDTO userDTO = new PostDTO.UserDTO();
            userDTO.setId(post.getUser().getId());
            userDTO.setUsername(post.getUser().getUsername());
            
            // 转换 Info
            if (post.getUser().getInfo() != null) {
                PostDTO.UserDTO.InfoDTO infoDTO = new PostDTO.UserDTO.InfoDTO();
                infoDTO.setId(post.getUser().getInfo().getId());
                infoDTO.setRealname(post.getUser().getInfo().getRealname());
                infoDTO.setAvatar(post.getUser().getInfo().getAvatar());
                infoDTO.setAge(post.getUser().getInfo().getAge());
                infoDTO.setGender(post.getUser().getInfo().getGender());
                infoDTO.setPhone(post.getUser().getInfo().getPhone());
                infoDTO.setEmail(post.getUser().getInfo().getEmail());
                userDTO.setInfo(infoDTO);
            }
            
            dto.setUser(userDTO);
        }
        
        // 转换 SeekerInfo
        if (post.getSeekerInfo() != null) {
            PostDTO.SeekerInfoDTO seekerInfoDTO = new PostDTO.SeekerInfoDTO();
            seekerInfoDTO.setId(post.getSeekerInfo().getId());
            seekerInfoDTO.setUserId(post.getSeekerInfo().getUserId());
            seekerInfoDTO.setEducation(post.getSeekerInfo().getEducation());
            seekerInfoDTO.setSchool(post.getSeekerInfo().getSchool());
            seekerInfoDTO.setFavor(post.getSeekerInfo().getFavor());
            seekerInfoDTO.setMembership(post.getSeekerInfo().getMembership());
            dto.setSeekerInfo(seekerInfoDTO);
        }
        
        // 转换 Resume
        if (post.getResume() != null) {
            PostDTO.ResumeDTO resumeDTO = new PostDTO.ResumeDTO();
            resumeDTO.setId(post.getResume().getId());
            resumeDTO.setUserId(post.getResume().getUserId());
            resumeDTO.setContent(post.getResume().getContent());
            resumeDTO.setAvatar(post.getResume().getAvatar());
            resumeDTO.setCreateTime(post.getResume().getCreateTime());
            dto.setResume(resumeDTO);
        }
        
        return dto;
    }
}
