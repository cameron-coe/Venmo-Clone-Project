package com.techelevator.tenmo.services;


import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ConsoleService {

    private final Scanner scanner = new Scanner(System.in);

    public int promptForMenuSelection(String prompt) {
        int menuSelection;
        System.out.print(prompt);
        try {
            menuSelection = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            menuSelection = -1;
        }
        return menuSelection;
    }

    public void printGreeting() {
        System.out.println("*********************");
        System.out.println("* Welcome to TEnmo! *");
        System.out.println("*********************");
    }

    public void printLoginMenu() {
        System.out.println();
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.println();
    }

    public void printMainMenu() {
        System.out.println();
        System.out.println("1: View your current balance");
        System.out.println("2: View your past transfers");
        System.out.println("3: View your pending requests");
        System.out.println("4: Send TE bucks");
        System.out.println("5: Request TE bucks");
        System.out.println("0: Exit");
        System.out.println();
    }

    public UserCredentials promptForCredentials() {
        String username = promptForString("Username: ");
        String password = promptForString("Password: ");
        return new UserCredentials(username, password);
    }

    public String promptForString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public int promptForInt(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    public BigDecimal promptForBigDecimal(String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                return new BigDecimal(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a decimal number.");
            }
        }
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public void printErrorMessage() {
        System.out.println("An error occurred. Check the log for details.");
    }

    public void printUserIdAndUsername(User user){
        String id = "" + user.getId();
        String username = user.getUsername();
        System.out.println(String.format("%-10s " + username, id ));
    }

    public int enterUserId(){
        System.out.print("Enter User ID: ");
        return getIntFromScanner();
    }

    public int enterTransactionId(){
        System.out.print("Please enter transfer ID to view details (0 to cancel): ");
        return getIntFromScanner();
    }

    public int enterPendingTransactionId(){
        System.out.print("Please enter transfer ID to approve/reject (0 to cancel): ");
        return getIntFromScanner();
    }

    public int getIntFromScanner(){
        String userInput = scanner.nextLine();
        try{
            int accountId = Integer.parseInt(userInput);
            return accountId;

        } catch (NumberFormatException e){
            System.out.println("Invalid Input");
        }
        return 0;
    }


    public float enterDollarAmount(){
        System.out.print("Enter Amount: ");
        String userInput = scanner.nextLine();

        try{
            float amount = Float.parseFloat(userInput);
            if (amount > 0){
                return amount;
            } else{
                System.out.println("Input must be greater than 0");
            }
        } catch (NumberFormatException e){
            System.out.println("Invalid Input");
        }
        return 0;
    }

    public void listSelectableUsers(User[] users){
        System.out.println("--------------------------------------");
        System.out.println("Users");
        String s = String.format("%-10s Name", "ID");
        System.out.println(s);
        System.out.println("--------------------------------------");

        for (User user : users){
            printUserIdAndUsername(user);
        }
    }

    public void sendBucksPage(User[] users) {
        listSelectableUsers(users);

        System.out.println("---------------------");
        System.out.println("");
        System.out.println("Enter ID of User you are sending to (0 to cancel): ");

    }

    public void requestBucksPage(User[] users) {
        listSelectableUsers(users);

        System.out.println("---------------------");
        System.out.println("");
        System.out.println("Enter ID of User you are requesting from (0 to cancel): ");


    }
    public void viewTransfersPage(List<Transaction> transactions, User currentUser) {
        System.out.println("--------------------------------------");
        System.out.println("Transfers");
        String s = String.format("%-10s %-15s Amount", "ID", "From/To");
        System.out.println(s);
        System.out.println("--------------------------------------");
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                printTransactionLine(transaction, currentUser);
            }
        }
        System.out.println("---------------------");
    }

    private void printTransactionLine(Transaction transaction, User user) {
        String transferId = "" + transaction.getTransferId();

        String senderOrRecipient = "";
        if(user.getId() == transaction.getSenderUserId()){
            senderOrRecipient = "To:   ";
            senderOrRecipient += transaction.getReceiverUsername();
        } else if (user.getId() == transaction.getReceiverUserId()){
            senderOrRecipient = "From: ";
            senderOrRecipient += transaction.getSenderUsername();
        }

        String s = String.format("%-10s %-15s $%1.2f", transferId, senderOrRecipient, transaction.getAmount());
        System.out.println(s);
    }

    public void printTransactionDetails(Transaction transaction){
        System.out.println("--------------------------------------");
        System.out.println("Transfer Details");
        System.out.println("--------------------------------------");
        System.out.println("ID:     " + transaction.getTransferId());
        System.out.println("From:   " + transaction.getSenderUsername());
        System.out.println("To:     " + transaction.getReceiverUsername());
        if (transaction.getTransferTypeId() == 1) {
            System.out.println("Type:   Request");
        } else if (transaction.getTransferTypeId() == 2){
            System.out.println("Type:   Send");
        }

        if (transaction.getTransferStatusId() == 1) {
            System.out.println("Status: Pending");
        } else if (transaction.getTransferTypeId() == 2){
            System.out.println("Status: Approved");
        }else if (transaction.getTransferTypeId() == 3){
            System.out.println("Status: Rejected");
        }

        System.out.println(String.format("Amount: $%1.2f", transaction.getAmount()));
    }

    public int approveOrRejectPendingTransfer() {
        System.out.println("1: Approve");
        System.out.println("2: Reject");
        System.out.println("0: Don't Approve Or Reject");


        System.out.println("---------------------");
        System.out.println("Please choose an option: ");
        return getIntFromScanner();
    }

    public void transactionCanceled(){
        System.out.println("Transaction Canceled");
    }

    public void transactionApproved(){
        System.out.println("Transaction Approved!");
    }

    public void transactionRejected(){
        System.out.println("Transaction Rejected!");
    }

    public void transactionErrorMessage(){
        System.out.println("There was a problem processing your transaction");
    }

    public void pendingSentMessage(){
        System.out.println("Your request has been sent! Please wait for approval. ");
    }

    public void noOptionSelected(){
        System.out.println("No option selected ");
    }
}
