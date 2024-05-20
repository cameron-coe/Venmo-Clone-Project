package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransactionDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class TransactionController {
    private TransactionDao transactionDao;
    private AccountDao accountDao;
    private UserDao userDao;

    public TransactionController(AccountDao accountDao, TransactionDao transactionDao, UserDao userDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.userDao = userDao;
    }

    @RequestMapping(path = "/sendto", method = RequestMethod.POST)
    public void makeTransaction(@Valid @RequestBody Transaction transaction, Principal principal){
        int fromUserId = accountDao.getUserIdByUsername(principal.getName());

        int receiverUserId = transaction.getReceiverUserId();
        float amount = transaction.getAmount();

        transactionDao.transferTo(fromUserId, receiverUserId, amount);
    }

    @RequestMapping(path = "/transactionhistory", method = RequestMethod.GET)
    public Transaction[] transactionHistory(Principal principal) {
        List<Transaction> transactionList = transactionDao.getTransactionsByUsername(principal.getName());
        Transaction[] transactionHistory = transactionList.toArray(new Transaction[transactionList.size()]);

        for(Transaction transaction : transactionHistory){
            User sender = userDao.getUserByAccountId(transaction.getFromAccountId());
            transaction.setSenderUserId(sender.getId());
            transaction.setSenderUsername(sender.getUsername());

            User receiver = userDao.getUserByAccountId(transaction.getToAccountId());
            transaction.setReceiverUserId(receiver.getId());
            transaction.setReceiverUsername(receiver.getUsername());
        }

        return transactionHistory;
    }

    @RequestMapping(path = "/request", method = RequestMethod.POST)
    public int requestTransfer(@Valid @RequestBody Transaction transaction){
        addAccountIdsByUserIds(transaction);
        return transactionDao.createRequest(transaction);
    }

    @RequestMapping(path = "/approve", method = RequestMethod.POST)
    public int approvePendingTransfer(@Valid @RequestBody Transaction transaction){
        transaction = addAccountIdsByUserIds(transaction);
        return transactionDao.approvePendingTransaction(transaction);
    }

    @RequestMapping(path = "/reject", method = RequestMethod.POST)
    public int rejectPendingTransfer(@Valid @RequestBody Transaction transaction){
        transaction = addAccountIdsByUserIds(transaction);
        return transactionDao.rejectPendingTransaction(transaction);
    }

    private Transaction addAccountIdsByUserIds(Transaction transaction){
        int toAccountId = userDao.getAccountIdByUserId(transaction.getReceiverUserId());
        transaction.setToAccountId(toAccountId);

        int fromAccountId = userDao.getAccountIdByUserId(transaction.getSenderUserId());
        transaction.setFromAccountId(fromAccountId);

        return transaction;
    }

}
