package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transaction;

import java.util.List;


public interface TransactionDao {
    boolean transferTo(int fromUserId, int toUserId, float amount);

    List<Transaction> getTransactionsByUsername(String username);

    int createRequest(Transaction transaction);
    int approvePendingTransaction(Transaction transaction);
    int rejectPendingTransaction(Transaction transaction);

}
