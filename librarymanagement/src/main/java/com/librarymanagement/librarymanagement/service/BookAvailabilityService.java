package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BookAvailabilityUpdateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class BookAvailabilityService {
    private static final Logger logger = LoggerFactory.getLogger(BookAvailabilityService.class);
    
    // Using a Many sink to broadcast to multiple subscribers
    private final Sinks.Many<BookAvailabilityUpdateDto> bookAvailabilitySink;
    private final Flux<BookAvailabilityUpdateDto> bookAvailabilityFlux;
    
    public BookAvailabilityService() {
        // Create a sink that allows multiple subscribers and retains the last emitted element
        this.bookAvailabilitySink = Sinks.many().replay().latest();
        this.bookAvailabilityFlux = bookAvailabilitySink.asFlux();
        logger.info("Real-time book availability service initialized");
    }
    
    /**
     * Publish an update to book availability
     * @param bookId The ID of the book
     * @param title The title of the book
     * @param available Whether the book is available
     */
    public void publishAvailabilityUpdate(Long bookId, String title, boolean available) {
        logger.info("Publishing availability update for book ID {}: available = {}", bookId, available);
        BookAvailabilityUpdateDto update = new BookAvailabilityUpdateDto(bookId, title, available);
        bookAvailabilitySink.tryEmitNext(update);
    }
    
    /**
     * Get a flux of book availability updates
     * @return Flux of book availability updates
     */
    public Flux<BookAvailabilityUpdateDto> getAvailabilityUpdates() {
        return bookAvailabilityFlux;
    }
} 