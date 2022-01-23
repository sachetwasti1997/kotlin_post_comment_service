package com.sachet.post_comment_microservice.controller

import com.sachet.post_comment_microservice.model.PostComment
import com.sachet.post_comment_microservice.repository.PostCommentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.time.LocalDateTime
import java.time.Month
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
internal class PostCommentControllerTest
@Autowired
constructor(
    val postCommentRepository: PostCommentRepository,
    val webTestClient: WebTestClient
) {

    val id = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        val commentsList = listOf<PostComment>(
            PostComment(
                commentId = id,
                postId = UUID.randomUUID().toString(),
                comment = "This is test 1 comment",
                dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
            ),
            PostComment(
                postId = UUID.randomUUID().toString(),
                comment = "This is test 2 comment",
                dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
            ),
            PostComment(
                postId = UUID.randomUUID().toString(),
                comment = "This is test 3 comment",
                dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
            ),
            PostComment(
                postId = id,
                comment = "This is test 4 comment",
                dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
            ),
            PostComment(
                postId = id,
                comment = "This is test 5 comment",
                dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
            )
        )
        runBlocking {
            withContext(Dispatchers.IO) {
                postCommentRepository.saveAll(commentsList).blockLast()
            }
        }
    }

    @AfterEach
    fun tearDown() {
        runBlocking {
            withContext(Dispatchers.IO) {
                postCommentRepository.deleteAll().block()
            }
        }
    }

    @Test
    fun savePostComment() {

        val postComment = PostComment(
            postId = UUID.randomUUID().toString(),
            comment = "This is test 6 comment",
            dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
        )

        webTestClient
            .post()
            .uri("/api/v1/post_comment/save")
            .bodyValue(postComment)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(PostComment::class.java)
            .consumeWith {
                val postCommentSaved = it.responseBody
                Assertions.assertNotNull(postCommentSaved?.commentId)
            }
    }

    @Test
    fun savePostCommentBadRequest() {

        val postComment = PostComment(
            dateCreated = LocalDateTime.of(2022, Month.FEBRUARY, 21, 0,0)
        )

        webTestClient
            .post()
            .uri("/api/v1/post_comment/save")
            .bodyValue(postComment)
            .exchange()
            .expectStatus()
            .isBadRequest
            .expectBody(String::class.java)
            .consumeWith {
                val message = it.responseBody
                message?.contains("errorCode")?.let { it1 -> Assertions.assertTrue(it1) }
                message?.contains("BAD_REQUEST")?.let { it1 -> Assertions.assertTrue(it1) }
            }
    }

    @Test
    fun getAllCommentsForPost(){
        webTestClient
            .get()
            .uri("/api/v1/post_comment/$id")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(PostComment::class.java)
            .consumeWith<WebTestClient.ListBodySpec<PostComment>> {
                val commentList = it.responseBody
                Assertions.assertEquals(2, commentList?.size)
            }
    }

    @Test
    fun getAllPostCommentPostNotFound(){
        webTestClient
            .get()
            .uri("/api/v1/post_comment/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(String::class.java)
            .consumeWith {
                val message = it.responseBody
                message?.contains("No comments found for the post")?.let { it1 -> Assertions.assertTrue(it1) }
                message?.contains("NOT_FOUND")?.let { it1 -> Assertions.assertTrue(it1) }
            }
    }

    @Test
    fun updateComment(){
        val comment = PostComment(
            postId = UUID.randomUUID().toString(),
            comment = "This is test One comment",
        )
        webTestClient
            .put()
            .uri("/api/v1/post_comment/$id")
            .bodyValue(comment)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(PostComment::class.java)
            .consumeWith {
                val commentUpdated = it.responseBody
                Assertions.assertEquals("This is test One comment", commentUpdated?.comment)
            }
    }

    @Test
    fun updateCommentNotFound(){
        val comment = PostComment(
            postId = UUID.randomUUID().toString(),
            comment = "This is test One comment",
        )
        webTestClient
            .put()
            .uri("/api/v1/post_comment/${UUID.randomUUID()}")
            .bodyValue(comment)
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(String::class.java)
            .consumeWith { itRes ->
                val message = itRes.responseBody
                message?.contains("errorCode")?.let { it1 -> Assertions.assertTrue(it1) }
                message?.contains("NOT_FOUND")?.let {
                    Assertions.assertTrue(it)
                }
            }
    }

    @Test
    fun deleteCommentWithId(){
        webTestClient
            .delete()
            .uri("/api/v1/post_comment/${id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .consumeWith {
                val message = it.responseBody
                Assertions.assertEquals("Successfully deleted the comment", message)
            }
    }

    @Test
    fun deleteCommentWithIdNotFound(){
        webTestClient
            .delete()
            .uri("/api/v1/post_comment/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(String::class.java)
            .consumeWith {
                val message = it.responseBody
                message?.contains("errorCode")?.let { it1 -> Assertions.assertTrue(it1) }
                message?.contains("NOT_FOUND")?.let { it1 -> Assertions.assertTrue(it1) }
            }
    }

    @Test
    fun deleteCommentOfPost(){
        webTestClient
            .delete()
            .uri("/api/v1/post_comment/post/${id}")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(String::class.java)
            .consumeWith {
                val message = it.responseBody
                Assertions.assertEquals("Successfully deleted all the comments on the post", message)
            }
    }

    @Test
    fun deleteCommentOfPostNotFound(){
        webTestClient
            .delete()
            .uri("/api/v1/post_comment/post/${UUID.randomUUID()}")
            .exchange()
            .expectStatus()
            .isNotFound
            .expectBody(String::class.java)
            .consumeWith {
                val message = it.responseBody
                message?.contains("errorCode")?.let { it1 -> Assertions.assertTrue(it1) }
                message?.contains("NOT_FOUND")?.let { it1 -> Assertions.assertTrue(it1) }
            }
    }

}












