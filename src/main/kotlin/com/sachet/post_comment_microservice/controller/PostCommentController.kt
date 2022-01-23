package com.sachet.post_comment_microservice.controller

import com.sachet.post_comment_microservice.model.PostComment
import com.sachet.post_comment_microservice.service.PostCommentCommentServiceImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/post_comment")
class PostCommentController(
    private val postCommentServiceImpl: PostCommentCommentServiceImpl
) {

    @PostMapping("/save")
    suspend fun savePostComment(@RequestBody postComment: Mono<PostComment>): ResponseEntity<PostComment> {
        val postCommentReceived = postComment.awaitSingle()
        return ResponseEntity(postCommentServiceImpl.addPostComment(postCommentReceived), HttpStatus.OK)
    }

    @GetMapping("/{postId}")
    suspend fun getCommentsForPost(
        @PathVariable postId:String,
        @RequestParam page:Long,
        @RequestParam size:Long) = ResponseEntity(postCommentServiceImpl.getPostCommentByPostId(postId, page, size), HttpStatus.OK)

    @PutMapping("/{commentId}")
    suspend fun updateComment(
        @PathVariable commentId: String,
        @RequestBody postComment: Mono<PostComment>
    ): ResponseEntity<PostComment>{
        val postCommentReceived = postComment.awaitSingle()
        return ResponseEntity(postCommentServiceImpl.updateComment(commentId, postCommentReceived), HttpStatus.OK)
    }

    @DeleteMapping("/{commentId}")
    suspend fun deleteComment(
        @PathVariable commentId: String
    ):ResponseEntity<String> {
        postCommentServiceImpl.deleteComment(commentId)
        return ResponseEntity("Successfully deleted the comment", HttpStatus.OK)
    }

    @DeleteMapping("/post/{postId}")
    suspend fun deleteAllCommentsOfPost(
        @PathVariable postId: String
    ):ResponseEntity<String> {
        postCommentServiceImpl.deleteAllCommentsOfPost(postId)
        return ResponseEntity("Successfully deleted all the comments on the post", HttpStatus.OK)
    }

}





















