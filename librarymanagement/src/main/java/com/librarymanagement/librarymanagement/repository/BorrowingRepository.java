package com.librarymanagement.librarymanagement.repository;

import com.librarymanagement.librarymanagement.model.Borrowing;
import com.librarymanagement.librarymanagement.model.BorrowingStatus;
import com.librarymanagement.librarymanagement.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    
    // Kullanıcının aktif olarak ödünç aldığı kitapları bulma (iade edilmemişler)
    List<Borrowing> findByUserAndStatusIn(User user, List<BorrowingStatus> statuses);
    
    // Kullanıcının belli durumdaki ödünç alma işlemleri (sayfalı)
    Page<Borrowing> findByUserAndStatusIn(User user, List<BorrowingStatus> statuses, Pageable pageable);
    
    // Sayfalı olarak kullanıcının ödünç aldığı kitaplar
    Page<Borrowing> findByUser(User user, Pageable pageable);
    
    // Belirli durumdaki tüm ödünç alma işlemleri
    Page<Borrowing> findByStatus(BorrowingStatus status, Pageable pageable);
    
    // Kullanıcının belirli durumdaki ödünç kayıtları (sayfalı)
    Page<Borrowing> findByUserAndStatus(User user, BorrowingStatus status, Pageable pageable);
    
    // Belirli durumdaki tüm ödünç alma işlemleri (liste olarak)
    List<Borrowing> findByStatus(BorrowingStatus status);
    
    // İade tarihi geçmiş ödünç alma işlemleri
    @Query("SELECT b FROM Borrowing b WHERE b.status = 'BORROWED' AND b.dueDate < :today")
    List<Borrowing> findOverdueBooks(LocalDate today);
    
    // Belirli bir kitap için aktif ödünç alma kaydı var mı
    Optional<Borrowing> findByBookIdAndStatusNot(Long bookId, BorrowingStatus status);
    
    // Kullanıcının aktif olarak kaç kitap ödünç aldığını saymak için
    int countByUserAndStatusIn(User user, List<BorrowingStatus> statuses);
} 