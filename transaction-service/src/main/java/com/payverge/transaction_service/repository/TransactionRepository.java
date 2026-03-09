package com.payverge.transaction_service.repository;

import com.payverge.transaction_service.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    List<Transaction> findBySenderIdOrReceiverId(Long senderId, Long receiverId);
}