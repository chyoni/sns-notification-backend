package cwchoiit.notification.consumer.application.port.out;

import cwchoiit.notification.consumer.application.model.Post;
import java.util.Optional;

public interface PostClientPort {
    Optional<Post> findPostById(Long postId);
}
