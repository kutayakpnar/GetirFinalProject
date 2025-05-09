package com.librarymanagement.librarymanagement.model;

public enum BorrowingStatus {
    BORROWED,   // Kitap ödünç alınmış, henüz iade edilmemiş
    RETURNED,   // Kitap iade edilmiş
    OVERDUE,    // İade tarihi geçmiş
    LOST        // Kitap kayıp/iade edilmemiş
} 