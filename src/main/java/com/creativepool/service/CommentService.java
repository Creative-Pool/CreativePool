package com.creativepool.service;

import com.creativepool.entity.Comment;
import com.creativepool.models.CommentRequest;
import com.creativepool.models.CommentResponse;
import com.creativepool.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public List<CommentResponse> getCommentsByPostId(UUID postId) {
        List<Comment> comments = commentRepository.findByTicketId(postId);
        List<Comment> rootComments = comments.stream()
                .filter(comment -> comment.getParentCommentId() == null)
                .collect(Collectors.toList());

        return rootComments.stream().map(comment -> toCommentResponse(comment, comments)).collect(Collectors.toList());
    }

    public CommentResponse addComment(CommentRequest commentRequest) {
        Comment comment = new Comment();
        comment.setTicketId(commentRequest.getPostId());
        comment.setUserId(commentRequest.getUserId());
        comment.setParentCommentId(commentRequest.getParentCommentId());
        comment.setContent(commentRequest.getContent());
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        comment.setStatus("active");
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        Comment savedComment = commentRepository.save(comment);
        return toCommentResponse(savedComment);
    }

    public CommentResponse editComment(Long id, String content) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setContent(content);
        comment.setUpdatedAt(new Date());
        Comment updatedComment = commentRepository.save(comment);
        return toCommentResponse(updatedComment);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    public CommentResponse likeComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setLikeCount(comment.getLikeCount() + 1);
        Comment updatedComment = commentRepository.save(comment);
        return toCommentResponse(updatedComment);
    }

    public CommentResponse dislikeComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setDislikeCount(comment.getDislikeCount() + 1);
        Comment updatedComment = commentRepository.save(comment);
        return toCommentResponse(updatedComment);
    }

    public CommentResponse reportComment(Long id) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus("reported");
        Comment updatedComment = commentRepository.save(comment);
        return toCommentResponse(updatedComment);
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return toCommentResponse(comment, null);
    }

    private CommentResponse toCommentResponse(Comment comment, List<Comment> allComments) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(comment.getId());
        commentResponse.setPostId(comment.getTicketId());
        commentResponse.setUserId(comment.getUserId());
        commentResponse.setParentCommentId(comment.getParentCommentId());
        commentResponse.setContent(comment.getContent());
        commentResponse.setCreatedAt(comment.getCreatedAt());
        commentResponse.setUpdatedAt(comment.getUpdatedAt());
        commentResponse.setStatus(comment.getStatus());
        commentResponse.setLikeCount(comment.getLikeCount());
        commentResponse.setDislikeCount(comment.getDislikeCount());

        if (allComments != null) {
            List<CommentResponse> replies = allComments.stream()
                    .filter(c -> c.getParentCommentId() != null && c.getParentCommentId().equals(comment.getId()))
                    .map(c -> toCommentResponse(c, allComments))
                    .collect(Collectors.toList());
            commentResponse.setReplies(replies);
        }

        return commentResponse;
    }

}
