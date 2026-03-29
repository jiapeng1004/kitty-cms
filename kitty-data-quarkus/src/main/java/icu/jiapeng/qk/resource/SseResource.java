package icu.jiapeng.qk.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.jboss.logging.Logger;

import java.time.Instant;

/**
 * SSE：{@link jakarta.ws.rs.sse.Sse}。
 */
@Path("/api/v1/events2")
@Produces(MediaType.SERVER_SENT_EVENTS)
public class SseResource {

    private static final Logger LOG = Logger.getLogger(SseResource.class);


    @GET
    @Path("/monitor")
    public Response monitor(
            @QueryParam("eventType") String eventType,
            @QueryParam("operator") String operator,
            @Context Sse sse,
            @Context SseEventSink eventSink) {

        Thread.ofVirtual().start(() -> {
            try {
                OutboundSseEvent.Builder builder = sse.newEventBuilder();
                for (int i = 0; i < 10; i++) {
                    String data = "{\"eventType\":\"" + eventType + "\",\"operator\":\"" + operator + "\",\"timestamp\":\"" + Instant.now() + "\"}";
                    OutboundSseEvent event = builder.data(data).build();
                    eventSink.send(event);
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                LOG.error("Failed to send SSE event", e);
            }
        });
        return Response.ok().build();
    }
}
