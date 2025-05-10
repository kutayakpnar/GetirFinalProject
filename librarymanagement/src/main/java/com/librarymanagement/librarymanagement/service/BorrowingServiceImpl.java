package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BorrowBookRequestDto;
import com.librarymanagement.librarymanagement.dto.BorrowingResponseDto;
import com.librarymanagement.librarymanagement.exception.*;
import com.librarymanagement.librarymanagement.model.*;
import com.librarymanagement.librarymanagement.repository.BookRepository;
import com.librarymanagement.librarymanagement.repository.BorrowingRepository;
import com.librarymanagement.librarymanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowingServiceImpl.class);
    
    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookAvailabilityService bookAvailabilityService;
    
    @Value("${app.borrowing.max-books:5}")
    private int maxBooksPerUser;
    
    @Value("${app.borrowing.default-period:14}")
    private int defaultBorrowingPeriodInDays;

    @Override
    @Transactional
    public BorrowingResponseDto borrowBook(Long userId, BorrowBookRequestDto requestDto) {
        logger.debug("Processing book borrowing request: User ID: {}, Book ID: {}", 
                userId, requestDto.getBookId());
        
        try {
            // Kullanıcıyı bul
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            logger.debug("User found for borrowing: ID: {}, Name: {} {}, Role: {}", 
                    user.getId(), user.getFirstName(), user.getLastName(), user.getRole());

            // Kullanıcının rolünü kontrol et - sadece PATRON rolü kitap ödünç alabilir
            if (user.getRole() != Role.PATRON) {
                logger.warn("Borrowing rejected - User is not a PATRON: ID: {}, Role: {}", userId, user.getRole());
                throw new IllegalArgumentException("Only patrons can borrow books");
            }

            // Kitabı bul
            Book book = bookRepository.findById(requestDto.getBookId())
                    .orElseThrow(() -> new BookNotFoundException("Book not found with id: " + requestDto.getBookId()));
            
            logger.debug("Book found for borrowing: ID: {}, Title: {}, Available: {}", 
                    book.getId(), book.getTitle(), book.getAvailable());

            // Kitabın müsait olup olmadığını kontrol et
            if (!book.getAvailable()) {
                // Kitap için aktif bir ödünç alma kaydı var mı kontrol et
                Optional<Borrowing> activeBorrowing = borrowingRepository.findByBookIdAndStatusNot(
                        book.getId(), BorrowingStatus.RETURNED);
                
                if (activeBorrowing.isPresent()) {
                    logger.warn("Book not available for borrowing: ID: {}, Title: {}, Currently borrowed by User ID: {}", 
                            book.getId(), book.getTitle(), activeBorrowing.get().getUser().getId());
                    throw new BookNotAvailableException("Book with id " + book.getId() + " is not available for borrowing");
                } else {
                    // Kitabın available durumu yanlış olabilir, düzeltelim
                    logger.info("Fixing incorrect book availability status: Book ID: {}, Title: {}", 
                            book.getId(), book.getTitle());
                    book.setAvailable(true);
                }
            }

            // Kullanıcının aktif borçlanma sayısını kontrol et (BORROWED veya OVERDUE durumunda)
            List<BorrowingStatus> activeStatuses = Arrays.asList(BorrowingStatus.BORROWED, BorrowingStatus.OVERDUE);
            int activeBorrowingsCount = borrowingRepository.countByUserAndStatusIn(user, activeStatuses);
            
            logger.debug("User's active borrowing count: {}, Maximum allowed: {}", 
                    activeBorrowingsCount, maxBooksPerUser);
            
            if (activeBorrowingsCount >= maxBooksPerUser) {
                logger.warn("Borrowing limit exceeded: User ID: {}, Active borrowings: {}, Limit: {}", 
                        userId, activeBorrowingsCount, maxBooksPerUser);
                throw new BorrowingLimitExceededException("User has reached the maximum borrowing limit of " + maxBooksPerUser + " books");
            }

            // İade tarihi belirle
            LocalDate borrowDate = LocalDate.now();
            LocalDate dueDate = requestDto.getDueDate() != null 
                    ? requestDto.getDueDate() 
                    : borrowDate.plusDays(defaultBorrowingPeriodInDays);
            
            logger.debug("Creating borrowing with borrow date: {}, due date: {}", borrowDate, dueDate);

            // Yeni borçlanma kaydı oluştur
            Borrowing borrowing = Borrowing.builder()
                    .user(user)
                    .book(book)
                    .borrowDate(borrowDate)
                    .dueDate(dueDate)
                    .status(BorrowingStatus.BORROWED)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Kitabı müsait değil olarak işaretle
            book.setAvailable(false);
            book.setUpdatedAt(LocalDateTime.now());
            bookRepository.save(book);
            
            // Publish real-time update about book availability
            bookAvailabilityService.publishAvailabilityUpdate(book.getId(), book.getTitle(), false);
            
            logger.debug("Book marked as unavailable: ID: {}, Title: {}", book.getId(), book.getTitle());

            // Borçlanma kaydını kaydet
            Borrowing savedBorrowing = borrowingRepository.save(borrowing);
            logger.info("Borrowing successfully created: ID: {}, User: {} {}, Book: {}, Due date: {}", 
                    savedBorrowing.getId(), user.getFirstName(), user.getLastName(), 
                    book.getTitle(), dueDate);

            // Yanıt DTO'yu oluştur ve döndür
            return mapToBorrowingResponseDto(savedBorrowing);
        } catch (UserNotFoundException | BookNotFoundException | BookNotAvailableException | BorrowingLimitExceededException e) {
            // Bu hataları loglama yapmıyoruz çünkü bu hataları çağıran metotlar zaten loglamıştır
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during book borrowing: User ID: {}, Book ID: {}", 
                    userId, requestDto.getBookId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public BorrowingResponseDto returnBook(Long userId, Long borrowingId) {
        logger.debug("Processing book return request: User ID: {}, Borrowing ID: {}", userId, borrowingId);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            logger.debug("User found for book return: ID: {}, Name: {} {}", 
                    user.getId(), user.getFirstName(), user.getLastName());

            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                    .orElseThrow(() -> new BorrowingNotFoundException("Borrowing not found with id: " + borrowingId));
            
            logger.debug("Borrowing found: ID: {}, Book: {}, Status: {}", 
                    borrowing.getId(), borrowing.getBook().getTitle(), borrowing.getStatus());

            // Borçlanma kaydı kullanıcıya ait mi kontrol et
            if (!borrowing.getUser().getId().equals(userId)) {
                logger.warn("Return rejected - Borrowing does not belong to user: Borrowing ID: {}, Borrowing User ID: {}, Requesting User ID: {}", 
                        borrowingId, borrowing.getUser().getId(), userId);
                throw new IllegalArgumentException("This borrowing record does not belong to the user");
            }

            // Borçlanma durumunu kontrol et - sadece BORROWED veya OVERDUE durumunda iade edilebilir
            if (borrowing.getStatus() != BorrowingStatus.BORROWED && borrowing.getStatus() != BorrowingStatus.OVERDUE) {
                logger.warn("Return rejected - Invalid borrowing status: Borrowing ID: {}, Current status: {}", 
                        borrowingId, borrowing.getStatus());
                throw new IllegalArgumentException("This book has already been returned or is in an invalid state");
            }

            // Borçlanma kaydını güncelle
            LocalDate returnDate = LocalDate.now();
            borrowing.setReturnDate(returnDate);
            borrowing.setStatus(BorrowingStatus.RETURNED);
            borrowing.setUpdatedAt(LocalDateTime.now());
            
            logger.debug("Updating borrowing status to RETURNED: ID: {}, Return date: {}", 
                    borrowing.getId(), returnDate);

            // Kitabı müsait olarak işaretle
            Book book = borrowing.getBook();
            book.setAvailable(true);
            book.setUpdatedAt(LocalDateTime.now());
            bookRepository.save(book);
            
            // Publish real-time update about book availability
            bookAvailabilityService.publishAvailabilityUpdate(
                    book.getId(), 
                    book.getTitle(), 
                    true);
            
            logger.debug("Book marked as available: ID: {}, Title: {}", book.getId(), book.getTitle());

            // Güncellenmiş borçlanma kaydını kaydet
            Borrowing updatedBorrowing = borrowingRepository.save(borrowing);
            
            logger.info("Book successfully returned: Borrowing ID: {}, User: {} {}, Book: {}, Return date: {}", 
                    updatedBorrowing.getId(), user.getFirstName(), user.getLastName(), 
                    book.getTitle(), returnDate);

            // Yanıt DTO'yu oluştur ve döndür
            return mapToBorrowingResponseDto(updatedBorrowing);
        } catch (UserNotFoundException | BorrowingNotFoundException e) {
            // Bu hataları loglama yapmıyoruz çünkü bu hataları çağıran metotlar zaten loglamıştır
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during book return: User ID: {}, Borrowing ID: {}", 
                    userId, borrowingId, e);
            throw e;
        }
    }

    @Override
    public Page<BorrowingResponseDto> getUserBorrowings(Long userId, Pageable pageable) {
        logger.debug("Retrieving all borrowings for user ID: {}, Page: {}, Size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            logger.debug("User found: ID: {}, Name: {} {}", 
                    user.getId(), user.getFirstName(), user.getLastName());

            Page<Borrowing> borrowings = borrowingRepository.findByUser(user, pageable);
            
            logger.debug("Retrieved {} borrowings for user ID: {} (Page {} of {})", 
                    borrowings.getNumberOfElements(), userId, 
                    borrowings.getNumber(), borrowings.getTotalPages());
            
            return borrowings.map(this::mapToBorrowingResponseDto);
        } catch (UserNotFoundException e) {
            logger.warn("Failed to retrieve borrowings - User not found: ID: {}", userId);
            throw e;
        }
    }

    @Override
    public Page<BorrowingResponseDto> getUserActiveBorrowings(Long userId, Pageable pageable) {
        logger.debug("Retrieving active borrowings for user ID: {}, Page: {}, Size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            logger.debug("User found: ID: {}, Name: {} {}", 
                    user.getId(), user.getFirstName(), user.getLastName());

            // Aktif borçlanmalar sadece BORROWED veya OVERDUE durumunda olanlardır
            List<BorrowingStatus> activeStatuses = Arrays.asList(BorrowingStatus.BORROWED, BorrowingStatus.OVERDUE);
            
            Page<Borrowing> activeBorrowings = borrowingRepository.findByUserAndStatusIn(
                user, activeStatuses, pageable);
            
            logger.debug("Retrieved {} active borrowings for user ID: {} (Page {} of {})", 
                    activeBorrowings.getNumberOfElements(), userId, 
                    activeBorrowings.getNumber(), activeBorrowings.getTotalPages());
                
            return activeBorrowings.map(this::mapToBorrowingResponseDto);
        } catch (UserNotFoundException e) {
            logger.warn("Failed to retrieve active borrowings - User not found: ID: {}", userId);
            throw e;
        }
    }

    @Override
    public BorrowingResponseDto getBorrowingById(Long borrowingId) {
        logger.debug("Retrieving borrowing by ID: {}", borrowingId);
        
        try {
            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                    .orElseThrow(() -> new BorrowingNotFoundException("Borrowing not found with id: " + borrowingId));
            
            logger.debug("Borrowing found: ID: {}, User: {} {}, Book: {}, Status: {}", 
                    borrowing.getId(), borrowing.getUser().getFirstName(), borrowing.getUser().getLastName(), 
                    borrowing.getBook().getTitle(), borrowing.getStatus());

            return mapToBorrowingResponseDto(borrowing);
        } catch (BorrowingNotFoundException e) {
            logger.warn("Borrowing not found: ID: {}", borrowingId);
            throw e;
        }
    }

    @Override
    public Page<BorrowingResponseDto> getBorrowingsByStatus(BorrowingStatus status, Pageable pageable) {
        logger.debug("Retrieving borrowings by status: {}, Page: {}, Size: {}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Borrowing> borrowings = borrowingRepository.findByStatus(status, pageable);
        
        logger.debug("Retrieved {} borrowings with status {}: Page {} of {}", 
                borrowings.getNumberOfElements(), status, 
                borrowings.getNumber(), borrowings.getTotalPages());
        
        return borrowings.map(this::mapToBorrowingResponseDto);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?") // Her gün gece yarısı çalış
    public void checkAndUpdateOverdueBooks() {
        logger.info("Running scheduled job to check and update overdue books");
        
        LocalDate today = LocalDate.now();
        List<Borrowing> overdueBorrowings = borrowingRepository.findOverdueBooks(today);
        
        logger.debug("Found {} overdue books to update", overdueBorrowings.size());
        
        if (!overdueBorrowings.isEmpty()) {
            int updatedCount = 0;
            
            for (Borrowing borrowing : overdueBorrowings) {
                borrowing.setStatus(BorrowingStatus.OVERDUE);
                borrowing.setUpdatedAt(LocalDateTime.now());
                borrowingRepository.save(borrowing);
                updatedCount++;
                
                logger.debug("Updated borrowing status to OVERDUE: ID: {}, Book: {}, User: {}, Due date: {}", 
                        borrowing.getId(), borrowing.getBook().getTitle(), 
                        borrowing.getUser().getEmail(), borrowing.getDueDate());
            }
            
            logger.info("Updated {} borrowings to OVERDUE status", updatedCount);
        } else {
            logger.info("No overdue books found for today: {}", today);
        }
    }

    @Override
    public Page<BorrowingResponseDto> getUserBorrowingHistory(Long userId, Pageable pageable) {
        logger.debug("Retrieving borrowing history for user ID: {}, Page: {}, Size: {}", 
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
            
            logger.debug("User found: ID: {}, Name: {} {}", 
                    user.getId(), user.getFirstName(), user.getLastName());

            // Kullanıcının RETURNED durumundaki (iade edilmiş) ödünç kayıtlarını getir
            Page<Borrowing> borrowingHistory = borrowingRepository.findByUserAndStatus(
                    user, BorrowingStatus.RETURNED, pageable);
            
            logger.debug("Retrieved {} returned borrowings for user ID: {} (Page {} of {})", 
                    borrowingHistory.getNumberOfElements(), userId, 
                    borrowingHistory.getNumber(), borrowingHistory.getTotalPages());
            
            return borrowingHistory.map(this::mapToBorrowingResponseDto);
        } catch (UserNotFoundException e) {
            logger.warn("Failed to retrieve borrowing history - User not found: ID: {}", userId);
            throw e;
        }
    }

    @Override
    public List<BorrowingResponseDto> getAllOverdueBooks() {
        logger.debug("Retrieving all overdue books");
        
        // Gecikmiş kitapların listesini getir (OVERDUE durumundaki tüm kayıtlar)
        List<Borrowing> overdueBooks = borrowingRepository.findByStatus(BorrowingStatus.OVERDUE);
        
        logger.debug("Retrieved {} overdue borrowings", overdueBooks.size());
        
        // DTO'lara dönüştür
        return overdueBooks.stream()
                .map(this::mapToBorrowingResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BorrowingResponseDto> getAllBorrowings(Pageable pageable) {
        logger.debug("Retrieving all borrowings, Page: {}, Size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        // Tüm ödünç kayıtlarını getir (duruma bakılmaksızın)
        Page<Borrowing> allBorrowings = borrowingRepository.findAll(pageable);
        
        logger.debug("Retrieved {} total borrowings (Page {} of {})", 
                allBorrowings.getNumberOfElements(), 
                allBorrowings.getNumber(), allBorrowings.getTotalPages());
        
        // DTO'lara dönüştür
        return allBorrowings.map(this::mapToBorrowingResponseDto);
    }

    private BorrowingResponseDto mapToBorrowingResponseDto(Borrowing borrowing) {
        return BorrowingResponseDto.builder()
                .id(borrowing.getId())
                .userId(borrowing.getUser().getId())
                .userName(borrowing.getUser().getFirstName() + " " + borrowing.getUser().getLastName())
                .bookId(borrowing.getBook().getId())
                .bookTitle(borrowing.getBook().getTitle())
                .bookAuthor(borrowing.getBook().getAuthor())
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .returnDate(borrowing.getReturnDate())
                .status(borrowing.getStatus())
                .build();
    }
} 