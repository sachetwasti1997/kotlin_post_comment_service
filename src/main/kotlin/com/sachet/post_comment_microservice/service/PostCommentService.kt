package com.sachet.post_comment_microservice.service

import com.sachet.post_comment_microservice.model.PostComment
import kotlinx.coroutines.flow.Flow

interface PostCommentService {

    suspend fun addPostComment(postComment: PostComment): PostComment
    suspend fun getPostCommentByPostId(postId: String, page:Long, size:Long): Flow<PostComment>
    suspend fun updateComment(commentId: String, postComment: PostComment): PostComment
    suspend fun deleteComment(commentId:String)
    suspend fun deleteAllCommentsOfPost(postId:String)

}