package com.iainschmitt.perdiction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    // TODO:
    // Integration tests for 400, 422, 201 return codes
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
