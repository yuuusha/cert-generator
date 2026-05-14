package com.diev.service;

import com.diev.entity.Event;
import com.diev.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private static final Path OUTPUT_ROOT = Paths.get("src/main/resources/output");

    @Transactional
    public Event createEvent(String name) {
        if (!eventRepository.isNameUnique(name)) {
            throw new IllegalArgumentException("Event with name '" + name + "' already exists");
        }

        String folderPath = createEventFolder(name);

        Event event = new Event();
        event.setName(name);
        event.setCreatedAt(Instant.now());
        event.setFolderPath(folderPath);

        Long eventId = eventRepository.addEvent(event);
        event.setId(eventId);

        return event;
    }

    private String createEventFolder(String eventName) {
        try {
            Files.createDirectories(OUTPUT_ROOT);

            String sanitizedName = eventName.trim()
                    .replaceAll("[^\\p{L}\\p{Nd}\\s._-]", "")
                    .replaceAll("\\s+", "_");

            Path eventFolder = OUTPUT_ROOT.resolve(sanitizedName);
            int counter = 1;

            while (Files.exists(eventFolder)) {
                eventFolder = OUTPUT_ROOT.resolve(sanitizedName + "_" + counter);
                counter++;
            }

            Files.createDirectories(eventFolder);
            return eventFolder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create event folder for: " + eventName, e);
        }
    }

    public boolean isEventNameUnique(String name) {
        return eventRepository.isNameUnique(name);
    }

    public Event findEvent(Long id) {
        return eventRepository.findEvent(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
    }

    public List<Event> getAllEvents() {
        return eventRepository.getAllEvents();
    }
}