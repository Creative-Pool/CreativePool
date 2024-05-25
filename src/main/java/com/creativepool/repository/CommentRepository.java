package com.creativepool.repository;

import com.creativepool.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment,Long> {

    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    List<Comment> findByTicketId(UUID postId);
}
