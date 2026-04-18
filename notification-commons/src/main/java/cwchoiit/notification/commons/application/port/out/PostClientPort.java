package cwchoiit.notification.commons.application.port.out;

import cwchoiit.notification.commons.application.model.Post;
import java.util.Optional;

public interface PostClientPort {
    Optional<Post> findPostById(Long postId);
}
