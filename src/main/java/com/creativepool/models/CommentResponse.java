package com.creativepool.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private UUID id;
    private UUID postId;
    private UUID userId;
    private UUID parentCommentId;
    private String content;
    private Date createdAt;
    private Date updatedAt;
    private String status;
    private String imageLink;
    private String videoLink;
    private int likeCount;
    private int dislikeCount;
    private List<CommentResponse> replies;
}
