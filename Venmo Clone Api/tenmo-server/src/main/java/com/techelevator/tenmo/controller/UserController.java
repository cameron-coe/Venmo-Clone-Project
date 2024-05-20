package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class UserController {

    private UserDao userDao;

    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public User[] displayUsers(Principal principal) {
        List<User> userList= userDao.getUsersExceptCurrentUser(principal.getName());
        if(userList.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No Accounts Found");
        } else {
            User[] userArray = userList.toArray(new User[userList.size()]);
            return userArray;
        }
    }

}
