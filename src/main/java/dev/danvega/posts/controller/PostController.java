package dev.danvega.posts.controller;

import dev.danvega.posts.data.Post;
import dev.danvega.posts.exception.PostNotFoundException;
import dev.danvega.posts.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("")
    public List<Post> findAll() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    public Optional<Post> findById(@PathVariable Integer id) {
        return Optional.ofNullable(
                postRepository.findById(id)
                        .orElseThrow(PostNotFoundException::new));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("")
    public Post create(@RequestBody @Validated Post post){
        return postRepository.save(post);
    }

    @PutMapping("{id}")
    public Post update(@PathVariable Integer id,  @RequestBody @Validated Post post){
        Optional<Post> existingPost = postRepository.findById(id);
        if(existingPost.isPresent()){
            Post updatedPost = new Post(
                    existingPost.get().id(),
                    existingPost.get().userId(),
                    post.title(),
                    post.title(),
                    existingPost.get().version()
            );
            return postRepository.save(updatedPost);
        } else {
            throw new PostNotFoundException();
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable Integer id){
        Optional<Post> existingPost = postRepository.findById(id);
        if(existingPost.isPresent()){
            postRepository.deleteById(id);
        } else {
            throw new PostNotFoundException();
        }
    }
}
