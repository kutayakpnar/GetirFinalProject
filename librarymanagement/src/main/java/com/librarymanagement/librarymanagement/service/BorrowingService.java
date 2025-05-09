package com.librarymanagement.librarymanagement.service;

import com.librarymanagement.librarymanagement.dto.BorrowBookRequestDto;
import com.librarymanagement.librarymanagement.dto.BorrowingResponseDto;
import com.librarymanagement.librarymanagement.model.BorrowingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BorrowingService {
    // Kitap ödünç alma
    BorrowingResponseDto borrowBook(Long userId, BorrowBookRequestDto requestDto);
    
    // Kitap iade etme
    BorrowingResponseDto returnBook(Long userId, Long borrowingId);
    
    // Kullanıcının ödünç aldığı tüm kitapları görüntüleme
    Page<BorrowingResponseDto> getUserBorrowings(Long userId, Pageable pageable);
    
    // Kullanıcının aktif olarak ödünç aldığı kitapları görüntüleme
    Page<BorrowingResponseDto> getUserActiveBorrowings(Long userId, Pageable pageable);
    
    // Belirli bir ödünç alma işlemini görüntüleme
    BorrowingResponseDto getBorrowingById(Long borrowingId);
    
    // Belirli durumdaki tüm ödünç alma işlemlerini görüntüleme (örn: gecikmiş olanlar)
    Page<BorrowingResponseDto> getBorrowingsByStatus(BorrowingStatus status, Pageable pageable);
    
    // Gecikmiş kitapları kontrol etme ve güncelleme (scheduled task için)
    void checkAndUpdateOverdueBooks();
    
    // Kullanıcının geçmiş ödünç alma işlemlerini görüntüleme (tamamlanmış olanlar)
    Page<BorrowingResponseDto> getUserBorrowingHistory(Long userId, Pageable pageable);
    
    // Tüm gecikmiş kitapların listesini getir (raporlama için)
    List<BorrowingResponseDto> getAllOverdueBooks();
    
    // Tüm kullanıcıların ödünç alma geçmişini görüntüleme (kütüphaneciler için)
    Page<BorrowingResponseDto> getAllBorrowings(Pageable pageable);
} 