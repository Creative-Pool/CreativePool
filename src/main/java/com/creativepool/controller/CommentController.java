package com.creativepool.controller;

import com.creativepool.models.CommentRequest;
import com.creativepool.models.CommentResponse;
import com.creativepool.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public List<CommentResponse> getComments(@RequestParam UUID postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @PostMapping
    public CommentResponse addComment(@RequestBody CommentRequest commentRequest) {
        return commentService.addComment(commentRequest);
    }

    @PutMapping("/{id}")
    public CommentResponse editComment(@PathVariable Long id, @RequestBody String content) {
        return commentService.editComment(id, content);
    }

    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
    }

    @PostMapping("/{id}/like")
    public CommentResponse likeComment(@PathVariable Long id) {
        return commentService.likeComment(id);
    }

    @PostMapping("/{id}/dislike")
    public CommentResponse dislikeComment(@PathVariable Long id) {
        return commentService.dislikeComment(id);
    }

    @PostMapping("/{id}/report")
    public CommentResponse reportComment(@PathVariable Long id) {
        return commentService.reportComment(id);
    }
}

