package com.sachet.post_comment_microservice.service

import com.sachet.post_comment_microservice.custom_exception.PostCommentDataException
import com.sachet.post_comment_microservice.custom_exception.PostCommentNotFoundException
import com.sachet.post_comment_microservice.model.PostComment
import com.sachet.post_comment_microservice.repository.PostCommentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.stream.Collectors
import javax.validation.Validator

@Service
class PostCommentCommentServiceImpl(
    private val postCommentRepository: PostCommentRepository,
    private val validator: Validator
) : PostCommentService {

    override suspend fun addPostComment(postComment: PostComment): PostComment {
        validateRequest(postComment)
        postComment.dateCreated = LocalDateTime.now()
        return postCommentRepository.save(postComment).awaitSingle()
    }

    override suspend fun getPostCommentByPostId(postId: String, page:Long, size:Long): Flow<PostComment> {
        val postList = postCommentRepository
            .findByPostId(postId)
            .skip(page*size)
            .take(size)
            .asFlow()
        if (postList.toList().isEmpty()){
            throw PostCommentNotFoundException("No comments found for the post")
        }
        return postList
    }

    override suspend fun updateComment(commentId: String, postComment: PostComment): PostComment {
        val postCommentSaved = postCommentRepository.findById(commentId).awaitSingleOrNull()
        if (postCommentSaved == null){
            throw PostCommentNotFoundException("Comment not found")
        }
        postCommentSaved.comment = postComment.comment
        postCommentSaved.dateCreated = LocalDateTime.now()
        return addPostComment(postComment)
    }

    override suspend fun deleteComment(commentId: String) {
        val postComment = postCommentRepository.findById(commentId).awaitSingleOrNull()
        if (postComment == null){
            throw PostCommentNotFoundException("Comment Not Found")
        }
        postCommentRepository.delete(postComment).awaitSingleOrNull()
    }

    override suspend fun deleteAllCommentsOfPost(postId: String) {
        val postComments = postCommentRepository.findByPostId(postId).asFlow().toList()
        if (postComments.isEmpty()){
            throw PostCommentNotFoundException("No comments found for the post")
        }
        postCommentRepository.deleteAll(postComments).awaitSingleOrNull()
    }

    private fun validateRequest(postComment: PostComment){
        val constraints = validator.validate(postComment)
        if (constraints.size > 0){
            val constraintMessage = constraints
                .stream()
                .map {
                    it.message
                }
                .collect(Collectors.joining(", "))

            throw PostCommentDataException(constraintMessage)
        }
    }
}