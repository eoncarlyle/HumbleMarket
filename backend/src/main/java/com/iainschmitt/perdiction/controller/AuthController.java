package com.iainschmitt.perdiction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iainschmitt.perdiction.model.rest.AuthData;
import com.iainschmitt.perdiction.model.rest.LogInReturnData;
import com.iainschmitt.perdiction.model.rest.SignUpReturnData;
import com.iainschmitt.perdiction.service.AuthService;

@Validated
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login")
    public ResponseEntity<LogInReturnData> logIn(@RequestBody AuthData authData) {
        return new ResponseEntity<>(authService.logInUserAccount(authData), HttpStatus.OK);
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<SignUpReturnData> signUp(@RequestBody AuthData authData) {
        authData.validate();
        return new ResponseEntity<>(authService.createUserAccount(authData), HttpStatus.CREATED);
    }
}
