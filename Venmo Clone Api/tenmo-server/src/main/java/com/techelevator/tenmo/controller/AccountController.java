package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;

@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController{

    private AccountDao accountDao;
    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;
    }
    @RequestMapping(path = "/accountbalance", method = RequestMethod.GET)
    public double getBalance(Principal principal) {
        String username = principal.getName();
        Double balance = accountDao.getBalanceByUsername(username);
        if (balance == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account Balance Not Found");
        } else {
            return balance;
        }
    }

}