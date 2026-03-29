package icu.jiapeng.qk.resource;

import icu.jiapeng.qk.model.Event;
import icu.jiapeng.qk.model.EventReportRequest;
import icu.jiapeng.qk.model.EventReportResult;
import icu.jiapeng.qk.service.EventService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.bson.Document;

import java.time.Instant;
import java.util.List;

@Path("/api/v1/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    EventService eventService;

    @POST
    @RunOnVirtualThread
    public EventReportResult report(EventReportRequest request) {
        Event event = new Event();
        event.eventType = request.eventType();
        event.operator = request.operator();
        event.value = request.value();
        eventService.report(event);
        return EventReportResult.OK;
    }

    @GET
    @Path("/stats/range")
    public List<Document> statsRange(
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("eventTypes") List<String> eventTypes) {
        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);
        return eventService.statsByEventType(startTime, endTime, eventTypes);
    }

    @GET
    @Path("/stats/ranking")
    public List<Document> operatorRanking(
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("eventTypes") List<String> eventTypes) {
        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);
        return eventService.statsByOperator(startTime, endTime, eventTypes);
    }

    @GET
    @Path("/stats/trend")
    public List<Document> trend(
            @QueryParam("start") String start,
            @QueryParam("end") String end,
            @QueryParam("eventTypes") List<String> eventTypes,
            @QueryParam("groupBy") @DefaultValue("day") String groupBy) {
        Instant startTime = Instant.parse(start);
        Instant endTime = Instant.parse(end);
        return eventService.trend(startTime, endTime, eventTypes, groupBy);
    }
}
