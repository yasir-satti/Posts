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
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    final int NUMBER_OF_POSTS = 100;

    @Test
    void shouldFindAllPosts() {
        Post[] posts = testRestTemplate.getForObject("/api/posts", Post[].class);
        assertThat(posts.length).isEqualTo(NUMBER_OF_POSTS);
    }
}
