package com.creativepool.models;

import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequest {
    private UUID postId;
    private UUID userId;
    private UUID parentCommentId;
    private String content;
    private String imageLink;
    private String videoLink;
}
