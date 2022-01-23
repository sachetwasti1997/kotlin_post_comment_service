package com.sachet.post_comment_microservice.repository

import com.sachet.post_comment_microservice.model.PostComment
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface PostCommentRepository: ReactiveMongoRepository<PostComment, String> {
    fun findByPostId(postId: String):Flux<PostComment>
}