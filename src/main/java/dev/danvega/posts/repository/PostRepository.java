package dev.danvega.posts.repository;

import dev.danvega.posts.data.Post;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends  ListCrudRepository<Post, Integer> {
    Post findByTitle(String title);
}
