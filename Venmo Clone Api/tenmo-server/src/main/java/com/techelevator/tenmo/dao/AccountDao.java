package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {

    int getAccountIdByUsername(String username);
    int getUserIdByUsername(String username);

    double getBalanceByUsername(String username);
}
