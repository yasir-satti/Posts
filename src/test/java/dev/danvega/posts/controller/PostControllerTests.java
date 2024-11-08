package dev.danvega.posts.controller;

import dev.danvega.posts.data.Post;
import dev.danvega.posts.exception.PostNotFoundException;
import dev.danvega.posts.repository.PostRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.Optional;

import static java.lang.StringTemplate.STR;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PostController.class)
@AutoConfigureMockMvc
public class PostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostRepository postRepository;

    private List<Post> posts;

    @BeforeEach
    void setup() {
        posts = List.of(new Post(1, 1, "Hello, World!", "This is my first post.", null),
                new Post(2, 1, "Second Post", "This is my second post.", null));
    }

    @Test
    void shouldFindAllPosts() throws Exception {

        String jsonResponse = """
                [
                    {
                        "id":1,
                        "userid":1,
                        "title":"Hello, World!",
                        "body":"This is my first post.",
                        "version": null
                    },
                    {
                        "id":2,
                        "userid":1,
                        "title":"Second Post",
                        "body":"This is my second post.",
                        "version": null
                    }
                ]
                """;

        when(postRepository.findAll()).thenReturn(posts);

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void shouldFindPostIfGivenValidId() throws Exception {

        var post = posts.getFirst();
        var jsonResponse = STR."""
                            {
                                "id":\{post.id()},
                                "userid":\{post.userid()},
                                "title":"\{post.title()}",
                                "body":"\{post.body()}",
                                "version": null
                            }
            """;

        when(postRepository.findById(1))
                .thenReturn(Optional.ofNullable(posts.getFirst()));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));
    }

    @Test
    void shouldNotFindPostWhenGivenInvalidPostId() throws Exception {

        when(postRepository.findById(999))
                .thenThrow(PostNotFoundException.class);

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createNewPost() throws Exception {

        var post = new Post(2, 2, "new title", "new body", null);

        when(postRepository.save(post))
                .thenReturn(post);

        var jsonContent = STR."""
                                            {
                                                "id":\{post.id()},
                                                "userid":\{post.userid()},
                                                "title":"\{post.title()}",
                                                "body":"\{post.body()}",
                                                "version": null
                                            }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title", 
                        CoreMatchers.is(post.title())));
    }

    @Test
    void shouldNotCreatePostWhenTitleAndBodyAreEmpty() throws Exception {

        var post = new Post(2, 2, "", "", null);

        when(postRepository.save(post))
                .thenReturn(post);

        var jsonContent = STR."""
                                            {
                                                "id":\{post.id()},
                                                "userid":\{post.userid()},
                                                "title":"\{post.title()}",
                                                "body":"\{post.body()}",
                                                "version": null
                                            }
                """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdatePostWhenGivenValidPost() throws Exception {

        var updatedPost = new Post(2, 2, "this is updated post", "this is updated body", 1);

        when(postRepository.findById(2))
                .thenReturn(Optional.of(updatedPost));
        when(postRepository.save(updatedPost))
                .thenReturn(updatedPost);

        var jsonContent = STR."""
                                            {
                                                "id":\{updatedPost.id()},
                                                "userid":\{updatedPost.userid()},
                                                "title":"\{updatedPost.title()}",
                                                "body":"\{updatedPost.body()}",
                                                "version":"\{updatedPost.version()}"
                                            }
                """;

        mockMvc.perform(put("/api/posts/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdatePostWhenExistingPostNotFound() throws Exception {

        var updatedPost = new Post(2, 2, "this is updated post", "this is updated body", 1);

        when(postRepository.findById(2))
                .thenReturn(Optional.empty());

        var jsonContent = STR."""
                                            {
                                                "id":\{updatedPost.id()},
                                                "userid":\{updatedPost.userid()},
                                                "title":"\{updatedPost.title()}",
                                                "body":"\{updatedPost.body()}",
                                                "version":"\{updatedPost.version()}"
                                            }
                """;

        mockMvc.perform(put("/api/posts/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePostWhenGivenValidId() throws Exception {

        var existingPost = new Post(2, 2, "this is updated post", "this is updated body", 1);

        when(postRepository.findById(2))
                .thenReturn(Optional.of(existingPost));

        doNothing().when(postRepository).deleteById(2);

        mockMvc.perform(delete("/api/posts/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(postRepository, times(1)).deleteById(2);
    }

    @Test
    void shouldNotDeletePostWhenExistingPostNotFound() throws Exception {

        var updatedPost = new Post(2, 2, "this is updated post", "this is updated body", 1);

        when(postRepository.findById(2))
                .thenReturn(Optional.empty());

        var jsonContent = STR."""
                                            {
                                                "id":\{updatedPost.id()},
                                                "userid":\{updatedPost.userid()},
                                                "title":"\{updatedPost.title()}",
                                                "body":"\{updatedPost.body()}",
                                                "version":"\{updatedPost.version()}"
                                            }
                """;

        mockMvc.perform(delete("/api/posts/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound());
    }
}
