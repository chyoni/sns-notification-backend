package cwchoiit.notification.consumer.adapter.out;

import cwchoiit.notification.consumer.application.model.Post;
import cwchoiit.notification.consumer.application.port.out.PostClientPort;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 게시글 서버가 있다고 가정하고, 해당 서버에서 postId로 특정 Post를 조회하는 Client */
@Component
public class RestTemplatePostClient implements PostClientPort {

    private final Map<Long, Post> posts =
            Map.of(
                    1L, new Post(1L, 11L, "IMAGE_URL_1", "POST_CONTENT_1"),
                    2L, new Post(2L, 22L, "IMAGE_URL_2", "POST_CONTENT_2"),
                    3L, new Post(3L, 33L, "IMAGE_URL_3", "POST_CONTENT_3"));

    @Override
    public Optional<Post> findPostById(Long postId) {
        return Optional.ofNullable(posts.get(postId));
    }
}
