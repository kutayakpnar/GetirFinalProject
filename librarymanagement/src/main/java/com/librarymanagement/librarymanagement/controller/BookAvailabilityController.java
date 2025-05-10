package com.librarymanagement.librarymanagement.controller;

import com.librarymanagement.librarymanagement.dto.BookAvailabilityUpdateDto;
import com.librarymanagement.librarymanagement.service.BookAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/books/availability")
@RequiredArgsConstructor
@Tag(name = "Book Availability", description = "Real-time book availability updates")
public class BookAvailabilityController {

    private static final Logger logger = LoggerFactory.getLogger(BookAvailabilityController.class);
    
    private final BookAvailabilityService bookAvailabilityService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Stream book availability updates", 
               description = "Provides a Server-Sent Events stream of book availability updates in real-time")
    public Flux<BookAvailabilityUpdateDto> streamAvailabilityUpdates() {
        logger.info("Client connected to book availability stream");
        return bookAvailabilityService.getAvailabilityUpdates();
    }
} 