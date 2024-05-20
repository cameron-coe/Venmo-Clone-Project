package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Account {
    private int accountId;
    private double balance;

    public void setBalance(double balance) {
        this.balance = balance;
    }
    public double getBalance(){
        return this.balance;
    }

    public Account(int accountId) {
        this.accountId = accountId;
    }
}