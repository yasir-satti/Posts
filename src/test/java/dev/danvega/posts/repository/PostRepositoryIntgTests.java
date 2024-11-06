package dev.danvega.posts.repository;

import dev.danvega.posts.data.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJdbcTest
public class PostRepositoryIntgTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreDb = new PostgreSQLContainer<>("postgres:16.0");

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    void setup(){
        List<Post> posts = List.of(new Post(1, 1, "Hello, World!", "new body", 0));
        postRepository.saveAll(posts);
    }

    @Test
    void dbConnectionEstablished() {
        assertThat(postgreDb.isCreated()).isTrue();
        assertThat(postgreDb.isRunning()).isTrue();
    }

    @Test
    void shouldReturnPostByTitle() {
        Post post = postRepository.findByTitle("Hello, World!");
        assertThat(post).isNotNull();
        assertThat(post.title()).isEqualTo("Hello, World!");
    }
}
