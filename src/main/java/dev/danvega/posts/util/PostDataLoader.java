package dev.danvega.posts.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import dev.danvega.posts.repository.PostRepository;
import dev.danvega.posts.data.Posts;

import java.io.IOException;
import java.io.InputStream;

@Component()
class PostDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PostDataLoader.class);
    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;

    public PostDataLoader(ObjectMapper objectMapper, PostRepository postRepository) {
        this.objectMapper = objectMapper;
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(postRepository.count() == 0){
            String POSTS_JSON = "/data/posts.json";
            log.info("Loading posts into database from JSON: {}", POSTS_JSON);
            try (InputStream inputStream = TypeReference.class.getResourceAsStream(POSTS_JSON)) {
                Posts response = objectMapper.readValue(inputStream, Posts.class);
                postRepository.saveAll(response.posts());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read JSON data", e);
            }
        }
    }

}
