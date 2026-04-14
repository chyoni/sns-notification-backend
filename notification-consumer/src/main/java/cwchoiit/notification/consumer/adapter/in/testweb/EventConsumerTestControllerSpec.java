package cwchoiit.notification.consumer.adapter.in.testweb;

import cwchoiit.notification.consumer.adapter.in.event.comment.CommentEvent;
import cwchoiit.notification.consumer.adapter.in.event.follow.FollowEvent;
import cwchoiit.notification.consumer.adapter.in.event.like.LikeEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;

@Tag(name = "Event Consumer Call Test API")
public interface EventConsumerTestControllerSpec {

    @Operation(
            requestBody =
                    @RequestBody(
                            content = {
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        examples = {
                                            @ExampleObject(
                                                    name = "Comment Event",
                                                    value = COMMENT_EVENT_PAYLOAD)
                                        })
                            }))
    void comment(CommentEvent commentEvent);

    @Operation(
            requestBody =
                    @RequestBody(
                            content = {
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        examples = {
                                            @ExampleObject(
                                                    name = "Like Event",
                                                    value = LIKE_EVENT_PAYLOAD)
                                        })
                            }))
    void like(LikeEvent likeEvent);

    @Operation(
            requestBody =
                    @RequestBody(
                            content = {
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        examples = {
                                            @ExampleObject(
                                                    name = "Follow Event",
                                                    value = FOLLOW_EVENT_PAYLOAD)
                                        })
                            }))
    void follow(FollowEvent followEvent);

    String COMMENT_EVENT_PAYLOAD =
            """
            {
                "type": "ADD",
                "postId": 1,
                "userId": 2,
                "commentId": 3
            }
            """;

    String LIKE_EVENT_PAYLOAD =
            """
            {
                "type": "ADD",
                "postId": 1,
                "userId": 2,
                "createdAt": "2026-04-11T00:00:00"
            }
            """;

    String FOLLOW_EVENT_PAYLOAD =
            """
            {
                "type": "ADD",
                "userId": 1,
                "targetUserId": 2,
                "createdAt": "2026-04-12T00:00:00"
            }
            """;
}
