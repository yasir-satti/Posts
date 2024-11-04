package dev.danvega.posts.repository;

import dev.danvega.posts.data.Post;
import org.springframework.data.repository.ListCrudRepository;

public interface PostRepository extends  ListCrudRepository<Post, Integer> {
}
