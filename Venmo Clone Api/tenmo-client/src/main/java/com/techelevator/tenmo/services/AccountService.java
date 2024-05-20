package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.util.BasicLogger;
import org.springframework.http.*;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;


public class AccountService {
    private String authToken;
    private RestTemplate restTemplate = new RestTemplate();
    private static final String API_BASE_URL = "http://localhost:8080/accountbalance";

    public double getAccountBalance(){
         double balance = 0.0;
        HttpEntity<Void> entity = newEntity();
        try {
            ResponseEntity<Double> response = restTemplate.exchange(API_BASE_URL, HttpMethod.GET, entity, Double.class);
            balance = response.getBody();


        } catch (RestClientResponseException e) {
            BasicLogger.log(e.getRawStatusCode() + " : " + e.getStatusText());
        } catch (ResourceAccessException e){
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }
    public HttpEntity<Void> newEntity(){
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


}

