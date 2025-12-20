package dev.lhs.charity_backend.repository;

import dev.lhs.charity_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}

