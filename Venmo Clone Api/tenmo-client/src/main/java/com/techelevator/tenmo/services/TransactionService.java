package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionService {
    private String authToken;
    private RestTemplate restTemplate = new RestTemplate();
    private static final String API_BASE_URL = "http://localhost:8080";


    public Transaction sendTo(Transaction transaction) {

        try {
            restTemplate.exchange(API_BASE_URL + "/sendto", HttpMethod.POST, newTransactionEntity(transaction), Transaction.class);
            return transaction;
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return null;
    }

    public Transaction createRequest(Transaction transaction){

        try{
            restTemplate.exchange(API_BASE_URL + "/request", HttpMethod.POST, newTransactionEntity(transaction), int.class);
            return transaction;
        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return null;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = null;
        try{
            ResponseEntity<Transaction[]> response = restTemplate.exchange(API_BASE_URL + "/transactionhistory", HttpMethod.GET, newEntity(), Transaction[].class);
            transactions = Arrays.asList(response.getBody());

        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return transactions;
    }

    public List<Transaction> getAllPendingTransactions(User user) {
        List<Transaction> transactions = getAllTransactions();
        List<Transaction> outputList = new ArrayList<>();

        for (Transaction transaction : transactions){
            //add to output list if status == 1 (pending)
            if (transaction.getTransferStatusId() == 1){
                if (user.getId() == transaction.getSenderUserId()) {
                    outputList.add(transaction);
                }
            }
        }
        return outputList;
    }

    public Transaction approvePendingTransaction(Transaction transaction){

        try{
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL + "/approve", HttpMethod.POST, newTransactionEntity(transaction), int.class);
            if (response.getBody() == 1){
                return transaction;
            }

        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return null;
    }

    public Transaction rejectPendingTransaction(Transaction transaction){

        try{
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL + "/reject", HttpMethod.POST, newTransactionEntity(transaction), int.class);
            if (response.getBody() == 1){
                return transaction;
            }

        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return null;
    }

    public HttpEntity<Void> newEntity(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    public HttpEntity<Transaction> newTransactionEntity(Transaction transaction){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transaction, headers);
    }


    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
