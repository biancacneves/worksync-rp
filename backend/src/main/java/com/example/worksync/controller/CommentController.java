package com.example.worksync.controller;

import com.example.worksync.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.example.worksync.dto.requests.CommentDTO;
import com.example.worksync.model.User;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDTO>> listCommentsByTask(@PathVariable Long taskId) {
        List<CommentDTO> comments = commentService.listCommentsByTask(taskId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@RequestBody CommentDTO commentDTO,
                                                 @AuthenticationPrincipal User user) {

        CommentDTO newComment = commentService.addComment(commentDTO, user);
        return ResponseEntity.ok(newComment);
    }
    
    @DeleteMapping("/{commentId}")
     public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, 
                                               @AuthenticationPrincipal User user) {
         commentService.deleteComment(commentId, user);
         return ResponseEntity.noContent().build();
     }

}
