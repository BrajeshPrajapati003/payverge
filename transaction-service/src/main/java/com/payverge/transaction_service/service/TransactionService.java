package com.payverge.transaction_service.service;


import com.payverge.transaction_service.dto.TransferRequest;
import com.payverge.transaction_service.entity.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;


public interface TransactionService {

    Transaction createTransaction(Transaction transaction);
    List<Transaction> getAllTransactions();
}