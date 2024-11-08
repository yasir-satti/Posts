package dev.danvega.posts.controller;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;

import dev.danvega.posts.data.Post;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Testcontainers
@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostControllerIntgTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.0")
            .withDatabaseName("blog")
            .withUsername("blog")
            .withPassword("secret_password")
            .withReuse(true)
            .withExposedPorts(5432)
            .withCreateContainerCmdModifier(cmd -> cmd.withHostConfig(
                    new HostConfig()
                            .withPortBindings(new PortBinding(Ports.Binding.bindPort(5432),
                                    new ExposedPort(5432)))
            ))
            .waitingFor(
                    Wait.forListeningPort()
            );

    @Autowired
    TestRestTemplate testRestTemplate;

    @Test
    void shouldFindAllPosts() {
        Post[] posts = testRestTemplate.getForObject(
                "/api/posts",
                Post[].class);
        assertThat(posts.length).isEqualTo(101);
    }

    @Test
    void shouldFindPostWhenGivenValidPostId() {

        ResponseEntity<Post> response = testRestTemplate.exchange(
                "/api/posts/1",
                HttpMethod.GET,
                null,
                Post.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldThrowNotFoundWhenGivenInvalidPostId() {

        ResponseEntity<Post> response = testRestTemplate.exchange(
                "/api/posts/999",
                HttpMethod.GET,
                null,
                Post.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    @Rollback
    void shouldCreateNewPostWhenPostIsValid() {
        Post post = new Post(101, 1, "101 title", "101 body", null);

        ResponseEntity<Post> response = testRestTemplate.exchange(
                "/api/posts",
                HttpMethod.POST,
                new HttpEntity<Post>(post),
                Post.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("101 title");
        assertThat(response.getBody().userid()).isEqualTo(1);
        assertThat(response.getBody().body()).isEqualTo("101 body");
    }

    @Test
    void shouldNotCreateNewPostWhenValidationFails() {
        Post post = new Post(101,1,"","",null);

        ResponseEntity<Post> response = testRestTemplate.exchange(
                "/api/posts",
                HttpMethod.POST,
                new HttpEntity<Post>(post),
                Post.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldUpdatePostWhenPostIsValid() {
        ResponseEntity<Post> response = testRestTemplate.exchange(
                "/api/posts/99",
                HttpMethod.GET,
                null,
                Post.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Post existing = response.getBody();
        assertThat(existing).isNotNull();
        Post updated = new Post(existing.id(),existing.userid(),"NEW POST TITLE #1", "NEW POST BODY #1",existing.version());

        assertThat(updated.id()).isEqualTo(99);
        assertThat(updated.userid()).isEqualTo(10);
        assertThat(updated.title()).isEqualTo("NEW POST TITLE #1");
        assertThat(updated.body()).isEqualTo("NEW POST BODY #1");
    }

    @Test
    void shouldDeleteWithValidID() {
        ResponseEntity<Void> response = testRestTemplate.exchange(
                "/api/posts/88",
                HttpMethod.DELETE,
                null,
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
