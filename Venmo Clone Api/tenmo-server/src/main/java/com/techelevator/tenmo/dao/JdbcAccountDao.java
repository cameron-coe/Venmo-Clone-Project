package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Account;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int getAccountIdByUsername(String username) {
        Account account = getAccountByUserName(username);
        return account.getAccountId();
    }

    @Override
    public int getUserIdByUsername(String username) {
        Account account = getAccountByUserName(username);
        return account.getUserId();
    }

    @Override
    public double getBalanceByUsername(String username) {
        Account account = getAccountByUserName(username);
        return account.getBalance();
    }

    private Account getAccountByUserName(String username) {
        Account account = null;
        String sql = "SELECT a.account_id, a.user_id, a.balance " +
                "FROM account a " +
                "JOIN tenmo_user t ON t.user_id = a.user_id " +
                "WHERE t.username = ? ;";
        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, username);
            if (result.next()){
                account = mapRowToAccount(result);
            }
        } catch (CannotGetJdbcConnectionException e){
            throw new DaoException("unable to connect to server or database");
        }

        return account;
    }


    private Account mapRowToAccount(SqlRowSet rs){
        int accountId = rs.getInt("account_id");
        int userId = rs.getInt("user_id");
        float balance = rs.getFloat("balance");

        Account account = new Account(accountId, userId, balance);
        return account;
    }
}
