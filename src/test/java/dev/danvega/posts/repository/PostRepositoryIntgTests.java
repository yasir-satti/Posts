package dev.danvega.posts.repository;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import dev.danvega.posts.data.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostRepositoryIntgTests {

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
    PostRepository postRepository;

    @Test
    void dbConnectionEstablished() {
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void shouldReturnAllPosts() {
        List<Post> posts = postRepository.findAll();
        assertThat(posts).isNotNull();
        assertThat(posts.size()).isEqualTo(100);
    }
}
