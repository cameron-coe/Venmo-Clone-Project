package com.techelevator.tenmo;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.*;

import java.util.List;

public class App {

    public static final int MENU_EXIT = 0;
    public static final int LOGIN_MENU_REGISTER = 1;
    public static final int LOGIN_MENU_LOGIN = 2;
    public static final int MAIN_MENU_VIEW_BALANCE = 1;
    public static final int MAIN_MENU_VIEW_TRANSFER_HISTORY = 2;
    public static final int MAIN_MENU_VIEW_PENDING_REQUESTS = 3;
    public static final int MAIN_MENU_SEND_TE_BUCKS = 4;
    public static final int MAIN_MENU_REQUEST_TE_BUCKS = 5;

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;
    private AccountService accountService = new AccountService();
    private UserService userService = new UserService();
    private TransactionService transactionService = new TransactionService();



    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != MENU_EXIT && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == LOGIN_MENU_REGISTER) {
                handleRegister();
            } else if (menuSelection == LOGIN_MENU_LOGIN) {
                handleLogin();
            } else if (menuSelection != MENU_EXIT) {
                consoleService.printMessage("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        consoleService.printMessage("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            consoleService.printMessage("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
        // setting auth token on account service
        accountService.setAuthToken(currentUser.getToken());
        // setting auth token on userService
        userService.setAuthToken(currentUser.getToken());
        // setting auth token on TransactionService
        transactionService.setAuthToken(currentUser.getToken());
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != MENU_EXIT) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == MAIN_MENU_VIEW_BALANCE) {
                viewCurrentBalance();
            } else if (menuSelection == MAIN_MENU_VIEW_TRANSFER_HISTORY) {
                viewTransferHistory();
            } else if (menuSelection == MAIN_MENU_VIEW_PENDING_REQUESTS) {
                viewPendingRequests();
            } else if (menuSelection == MAIN_MENU_SEND_TE_BUCKS) {
                sendBucks();
            } else if (menuSelection == MAIN_MENU_REQUEST_TE_BUCKS) {
                requestBucks();
            } else if (menuSelection == MENU_EXIT) {
                continue;
            } else {
                consoleService.printMessage("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        float balance = (float) accountService.getAccountBalance();
		consoleService.printMessage(String.format("Current amount: $%1.2f", balance));
		
	}

	private void viewTransferHistory() {
        List<Transaction> tractionHistory = transactionService.getAllTransactions();
        consoleService.viewTransfersPage(tractionHistory, currentUser.getUser());

        // Get selected transaction
        int transactionId = consoleService.enterTransactionId();
        if (transactionId == 0) {
            consoleService.transactionCanceled();
        } else {
            for (Transaction transaction : tractionHistory) {
                if (transaction.getTransferId() == transactionId) {
                    consoleService.printTransactionDetails(transaction);
                    break;
                }
            }
        }
	}

	private void viewPendingRequests() {
        List<Transaction> tractionHistory = transactionService.getAllPendingTransactions(currentUser.getUser());
        consoleService.viewTransfersPage(tractionHistory, currentUser.getUser());

        int transactionId = consoleService.enterPendingTransactionId();
        if (transactionId == 0) {
            consoleService.transactionCanceled();
        } else {
            for (Transaction transaction : tractionHistory) {
                if (transaction.getTransferId() == transactionId) {
                    approveOrRejectPendingRequests(transaction);
                    break;
                }
            }
        }
	}

    private void approveOrRejectPendingRequests(Transaction transaction) {
        // TODO Auto-generated method stub

        int userSelection = consoleService.approveOrRejectPendingTransfer();
        if (userSelection == 1){
            // Approve
            Transaction approvedTransaction = transactionService.approvePendingTransaction(transaction);
            if (approvedTransaction == null){
                consoleService.transactionErrorMessage();
            } else {
                consoleService.transactionApproved();
            }

        } else if (userSelection == 2) {
            // Reject
            Transaction rejectedTransaction = transactionService.rejectPendingTransaction(transaction);
            if (rejectedTransaction == null){
                consoleService.transactionErrorMessage();
            } else {
                consoleService.transactionRejected();
            }
        } else {
            consoleService.noOptionSelected();
        }

    }

	private void sendBucks() {
        //List all users expect current user
        consoleService.sendBucksPage(userService.getUsernamesAndIdsExceptCurrentUser());
        Transaction transaction = new Transaction();

        int receiverUserId = consoleService.enterUserId();
        if (receiverUserId == 0) {
            consoleService.transactionCanceled();

        } else {
            float transactionAmount = consoleService.enterDollarAmount();

            if (transactionAmount == 0) {
                consoleService.transactionCanceled();

            } else {
                transaction.setReceiverUserId(receiverUserId);
                transaction.setAmount(transactionAmount);
                boolean transactionApproved = (transactionService.sendTo(transaction) != null );
                if (transactionApproved){
                    consoleService.transactionApproved();
                } else {
                    consoleService.transactionErrorMessage();
                }
            }
        }
	}

	private void requestBucks() {
        consoleService.requestBucksPage(userService.getUsernamesAndIdsExceptCurrentUser());

        int requesteeUserId = consoleService.enterUserId();
        if (requesteeUserId == 0){
            consoleService.transactionCanceled();
        } else {
            float transactionAmount = consoleService.enterDollarAmount();

            if (transactionAmount == 0) {
                consoleService.transactionCanceled();
            } else {
                Transaction transaction = new Transaction();
                transaction.setTransferTypeId(1); // 1 = request
                transaction.setTransferStatusId(1); // 1 = pending

                transaction.setReceiverUserId(currentUser.getUser().getId());
                transaction.setSenderUserId(requesteeUserId);
                transaction.setAmount(transactionAmount);

                boolean transactionApproved = (transactionService.createRequest(transaction) != null );
                if (transactionApproved){
                    consoleService.pendingSentMessage();
                } else {
                    consoleService.transactionErrorMessage();
                }
            }
        }
	}

}
