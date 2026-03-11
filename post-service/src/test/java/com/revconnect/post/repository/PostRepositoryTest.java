package com.revconnect.post.repository;

import com.revconnect.post.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("PostRepository Integration Tests")
class PostRepositoryTest {

    @Autowired PostRepository postRepository;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        postRepository.save(Post.builder().userId(1L).content("Post A").build());
        postRepository.save(Post.builder().userId(1L).content("Post B").build());
        postRepository.save(Post.builder().userId(2L).content("Post C").build());
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - returns posts for specific user")
    void findByUserId_returnsOnlyUserPosts() {
        Page<Post> page = postRepository.findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(2);
        page.getContent().forEach(p -> assertThat(p.getUserId()).isEqualTo(1L));
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc - returns empty page for user with no posts")
    void findByUserId_noPostsForUser_returnsEmpty() {
        Page<Post> page = postRepository.findByUserIdOrderByCreatedAtDesc(99L, PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("findByUserIdInOrderByCreatedAtDesc - returns posts for multiple user ids")
    void findByUserIdIn_returnsPostsForMultipleUsers() {
        Page<Post> page = postRepository.findByUserIdInOrderByCreatedAtDesc(
                List.of(1L, 2L), PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("findByUserIdInOrderByCreatedAtDesc - returns empty for empty user id list")
    void findByUserIdIn_emptyList_returnsEmpty() {
        Page<Post> page = postRepository.findByUserIdInOrderByCreatedAtDesc(
                List.of(), PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("save - persists post with auto-generated id and timestamps")
    void save_persistsWithTimestamps() {
        Post saved = postRepository.save(Post.builder().userId(3L).content("New post").build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
