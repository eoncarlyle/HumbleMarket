package com.iainschmitt.perdiction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iainschmitt.perdiction.model.User;
import com.iainschmitt.perdiction.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{userEmail}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String userEmail) {
        return ResponseEntity.ok(userService.getUserByEmail(userEmail));
    }
}
