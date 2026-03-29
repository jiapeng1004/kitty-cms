package icu.jiapeng.qk.service;

import icu.jiapeng.qk.model.Event;
import icu.jiapeng.qk.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class EventService {

    @Inject
    EventRepository eventRepository;

    public void report(Event event) {
        event.timestamp = Instant.now();
        event.createTime = Instant.now();
        eventRepository.persist(event);
    }

    public List<Document> statsByEventType(Instant start, Instant end, List<String> eventTypes) {
        return eventRepository.aggregateStatsByEventType(start, end, eventTypes);
    }

    public List<Document> statsByOperator(Instant start, Instant end, List<String> eventTypes) {
        return eventRepository.aggregateStatsByOperator(start, end, eventTypes);
    }

    public List<Document> trend(Instant start, Instant end, List<String> eventTypes, String groupBy) {
        return eventRepository.aggregateTrend(start, end, eventTypes, groupBy);
    }
}
