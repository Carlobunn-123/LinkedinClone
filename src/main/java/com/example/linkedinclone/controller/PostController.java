package com.example.linkedinclone.controller;

import com.example.linkedinclone.entity.Comment;
import com.example.linkedinclone.entity.Post;
import com.example.linkedinclone.service.PostService;
import com.example.linkedinclone.dto.PostUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import com.example.linkedinclone.repository.CommentRepository;


@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentRepository commentRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(@RequestBody Map<String, String> payload) {
        String content = payload.get("content");

        // Retrieve the logged-in username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Get the actual username from authentication

        // Set additional fields
        String avatarUrl = payload.getOrDefault("avatarUrl", "/images/avatar.jpg");
        String basicInfo = payload.getOrDefault("basicInfo", "Not provided");

        // Create and save the post
        Post newPost = new Post(content, username);
        newPost.setAvatarUrl(avatarUrl);
        newPost.setBasicInfo(basicInfo);
        newPost.setCreatedAt(LocalDateTime.now());

        Post savedPost = postService.savePost(newPost);

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedPost.getId().toString());
        response.put("avatarUrl", savedPost.getAvatarUrl());
        response.put("basicInfo", savedPost.getBasicInfo());
        response.put("createdAt", savedPost.getCreatedAt().toString());
        response.put("content", savedPost.getContent());
        response.put("username", savedPost.getUsername()); // Include username in the response

        return ResponseEntity.ok(response);
    }




    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable("id") Long id, @RequestBody PostUpdateRequest updateRequest) {
        Post updatedPost = postService.updatePost(id, updateRequest.getContent());
        if (updatedPost != null) {
            return ResponseEntity.ok(updatedPost);  // Return updated post
        } else {
            return ResponseEntity.notFound().build();  // Handle post not found case
        }

    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        boolean isDeleted = postService.deletePost(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build(); // Return 204 No Content if successful
        } else {
            return ResponseEntity.notFound().build(); // Return 404 Not Found if the post doesn't exist
        }
    }

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable Long postId) {
        // Retrieve the logged-in username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        postService.likePost(postId, username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId) {
        // Retrieve the logged-in username
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        postService.unlikePost(postId, username);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/{postId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable Long postId, @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Comment comment = postService.addComment(postId, username, content);

        Map<String, Object> response = new HashMap<>();
        response.put("id", comment.getId());
        response.put("username", comment.getUsername());
        response.put("content", comment.getContent());
        response.put("createdAt", comment.getCreatedAt().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        boolean isDeleted = postService.deleteComment(postId, commentId);
        if (isDeleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }




}
