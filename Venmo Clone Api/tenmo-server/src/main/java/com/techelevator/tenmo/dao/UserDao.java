package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.RegisterUserDto;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface UserDao {

    List<User> getUsers();

    List<User> getUsersExceptCurrentUser(String currentUsername);

    User getUserById(int id);

    User getUserByUsername(String username);
    User getUserByAccountId(int accountId);


    User createUser(RegisterUserDto user);
    int getUserIdByAccountId(int accountId);
    int getAccountIdByUserId(int userId);

}
