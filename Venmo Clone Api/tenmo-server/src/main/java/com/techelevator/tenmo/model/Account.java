package com.techelevator.tenmo.model;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class Account {
    @NotNull
    private int accountId;
    @NotNull
    private int userId;
    @NotNull
    private double balance;


    public Account(int accountId, int userId ,double balance) {
        this.accountId = accountId;
        this.userId = userId;
        this.balance = balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
