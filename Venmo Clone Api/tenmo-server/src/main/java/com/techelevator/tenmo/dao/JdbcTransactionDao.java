package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transaction;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class JdbcTransactionDao implements TransactionDao{

    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Transfers Money from one account to another
    @Override
    public boolean transferTo(int fromUserId, int toUserId, float amount) {
        String sql = "SELECT balance " +
                "FROM account " +
                "WHERE user_id = ?;";

        try {
            SqlRowSet result = jdbcTemplate.queryForRowSet(sql, fromUserId);
            float balance = 0;
            if (result.next()) {
                balance = result.getFloat("balance");
            }

            if (amount > balance) {
                throw new DaoException("Not Enough Funds In Account To Send");
            } else if (amount <= 0) {
                throw new DaoException("You Must Transfer An Amount Over Zero");
            } else if (fromUserId == toUserId) {
                throw new DaoException("Nice Try, Steve!");
            } else {
                // Transfer is allowed
                updateAccountAmount(fromUserId, toUserId, amount);
            }

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data Integrity Violation", e);
        }

        return true;
    }

    private void updateAccountAmount(int fromUserId, int toUserId, float amount) {
        String sql = "START TRANSACTION; ";

        sql += "UPDATE account " +
                    "SET balance = (SELECT balance " +
                        "FROM account " +
                        "WHERE user_id = ?) - ? " +
                    "WHERE user_id = ?; ";
        sql += "UPDATE account " +
                    "SET balance = (SELECT balance " +
                        "FROM account " +
                        "WHERE user_id = ?) + ? " +
                    "WHERE user_id = ?; ";

        sql += "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES (?, ?, " +
                        "(SELECT account_id FROM account WHERE user_id = ?), " +
                        "(SELECT account_id FROM account WHERE user_id = ?), " +
                        "?);";

        sql += "COMMIT;";


        try {
            int transferType = 2; //Send request transfer type
            int transferStatusId = 2; //approved

            int rowsUpdated = jdbcTemplate.update(sql, fromUserId, amount, fromUserId,
                    toUserId, amount, toUserId,
                    transferType, transferStatusId, fromUserId, toUserId, amount);

            //TODO ********************************************************
//            if (rowsUpdated == 0) {
//                throw new DaoException("Zero Rows Updated, Expected At Least One");
//            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data Integrity Violation", e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByUsername(String username) {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT t.transfer_id, t.transfer_type_id, t.transfer_status_id, t.account_from, t.account_to, t.amount " +
                "FROM tenmo_user tu " +
                "JOIN account a ON tu.user_id = a.user_id " +
                "JOIN transfer t ON (t.account_from = a.account_id) OR (t.account_to = a.account_id) " +
                "WHERE tu.username = ? AND t.transfer_status_id != 3;";  // don't show rejected transactions

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, username);
            while (results.next()){
                Transaction transaction = mapRowToTransaction(results);
                transactions.add(transaction);
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data Integrity Violation", e);
        }
        return transactions;
    }

    @Override
    public int createRequest(Transaction transaction) {
        int rowsAffected = 0;
        String sql = "INSERT INTO transfer(transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                    "VALUES(?, ?, ?, ?, ?) ;";
        try{
            if (transaction.getAmount() <= 0) {
                throw new DaoException("You Must Transfer An Amount Over Zero");
            } else if (transaction.getSenderUserId() == transaction.getReceiverUserId()) {
                throw new DaoException("Nice Try Steve!");
            } else {
                // request sent
                rowsAffected = jdbcTemplate.update(sql,
                    1,   //transfer_type_id = request
                    1,   //transfer_status_id = pending
                    transaction.getFromAccountId(),
                    transaction.getToAccountId(),
                    transaction.getAmount()
                );
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data Integrity Violation", e);
        }
        return rowsAffected;
    }

    public int approvePendingTransaction(Transaction transaction){
        int rowsUpdated = 0;
        String sql = "START TRANSACTION; ";

        sql += "UPDATE account " +
                "SET balance = (SELECT balance " +
                    "FROM account " +
                    "WHERE account_id = ?) - ? " +
                "WHERE account_id = ?; ";

        sql += "UPDATE account " +
                "SET balance = (SELECT balance " +
                    "FROM account " +
                    "WHERE account_id = ?) + ? " +
                "WHERE account_id = ?; ";

        sql += "UPDATE transfer " +
                "SET transfer_status_id = 2 " + // 2 = approved
                "WHERE transfer_id = ?; ";

        sql += "COMMIT;";

        String sqlToGetSenderAccountBalance = "SELECT balance " +
                "FROM account " +
                "WHERE account_id = ?;";
        try{
            int fromAccountId = transaction.getFromAccountId();
            int toAccountId = transaction.getToAccountId();
            float amount = transaction.getAmount();
            int transferId = transaction.getTransferId();

            SqlRowSet balanceCheck= jdbcTemplate.queryForRowSet(sqlToGetSenderAccountBalance, fromAccountId);
            float senderBalance = 0;
            if(balanceCheck.next()){
                senderBalance = balanceCheck.getFloat("balance");
            }

            if (amount > senderBalance) {
                throw new DaoException("Not Enough Funds In Account To Send");
            } else {
                // Transfer is allowed
                jdbcTemplate.update(sql, fromAccountId, amount, fromAccountId,
                        toAccountId, amount, toAccountId,
                        transferId);
                rowsUpdated = 1;
            }

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data Integrity Violation", e);
        }
        return rowsUpdated;
    }

    public int rejectPendingTransaction(Transaction transaction){
        int rowsUpdated = 0;
        String sql = "UPDATE transfer " +
                "SET transfer_status_id = 3 " + // 3 = rejected
                "WHERE transfer_id = ?; ";

        String sqlToGetSenderAccountBalance = "SELECT balance " +
                "FROM account " +
                "WHERE account_id = ?;";
        try{
            int fromAccountId = transaction.getFromAccountId();
            int toAccountId = transaction.getToAccountId();
            float amount = transaction.getAmount();
            int transferId = transaction.getTransferId();

            SqlRowSet balanceCheck= jdbcTemplate.queryForRowSet(sqlToGetSenderAccountBalance, fromAccountId);
            float senderBalance = 0;
            if(balanceCheck.next()){
                senderBalance = balanceCheck.getFloat("balance");
            }

            if (amount > senderBalance) {
                throw new DaoException("Not Enough Funds In Account To Send");
            } else {
                // Transfer is allowed
                jdbcTemplate.update(sql, transferId);
                rowsUpdated = 1;
            }

        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data Integrity Violation", e);
        }
        return rowsUpdated;
    }

    private Transaction mapRowToTransaction(SqlRowSet result){
        Transaction transaction = new Transaction();
        transaction.setTransferId(result.getInt("transfer_id"));
        transaction.setTransferTypeId(result.getInt("transfer_type_id"));
        transaction.setTransferStatusId(result.getInt("transfer_status_id"));
        transaction.setFromAccountId(result.getInt("account_from"));
        transaction.setToAccountId(result.getInt("account_to"));
        transaction.setAmount(result.getFloat("amount"));
        return transaction;
    }

}
