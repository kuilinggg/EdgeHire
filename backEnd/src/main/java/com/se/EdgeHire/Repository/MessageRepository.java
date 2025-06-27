package com.se.EdgeHire.Repository;

import com.se.EdgeHire.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    /**
     * 查询用户之间的对话记录
     * @param user1 对话参与者1
     * @param user2 对话参与者2
     * @return 返回两人之间的对话记录列表
     */
    @Query("SELECT m FROM Message m " +
            "WHERE (m.senderId = :user1 AND m.receiverId = :user2) " +
            "OR (m.senderId = :user2 AND m.receiverId = :user1) " +
            "ORDER BY m.time ASC")
    List<Message> findConversation(
            @Param("user1") int user1,
            @Param("user2") int user2
    );

    /**
     * 查询用户的所有消息
     * @param userId 用户ID
     * @return 返回用户的所有消息列表
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.senderId = :userId OR m.receiverId = :userId " +
            "ORDER BY m.time ASC")
    List<Message> findMessagesByUserId(@Param("userId") int userId);

    /**
     * 查询用户所有未读消息
     * @param receiverId 用户id
     * @param isRead 是否已读
     * @return 符合条件的消息列表
     */
    List<Message> findByReceiverIdAndIsRead(
            int receiverId, int isRead);

    /**
     * 查询用户发送给特定接收者的所有未读消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param isRead 消息是否已读（0表示未读，1表示已读）
     * @return 返回符合条件的消息列表
     */
    List<Message> findBySenderIdAndReceiverIdAndIsRead(
            int senderId, int receiverId, int isRead);


    /**
     * 查询用户的所有最新未读消息
     * @param userId 用户ID
     * @return 返回最新未读消息列表
     */
    @Query("SELECT m FROM Message m " +
            "WHERE m.id IN (" +
            "   SELECT MAX(m2.id) FROM Message m2 " +
            "   WHERE m2.receiverId = :userId AND m2.isRead = 0 " +
            "   GROUP BY m2.senderId" +
            ")")
    List<Message> findLatestUnreadMessages(@Param("userId") int userId);

    /**
     * 查询所有与当前用户发生过对话的用户
     * @param userId 用户ID
     * @return 返回用户的所有聊天伙伴ID列表
     */
    @Query("SELECT DISTINCT " +
            "CASE " +
            "    WHEN m.senderId = :userId THEN m.receiverId " +
            "    ELSE m.senderId " +
            "END " +
            "FROM Message m " +
            "WHERE m.senderId = :userId OR m.receiverId = :userId")
    List<Integer> findAllChatUsers(@Param("userId") int userId);
}
