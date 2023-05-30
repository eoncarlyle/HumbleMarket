package com.iainschmitt.perdiction.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iainschmitt.perdiction.model.Position;
import com.iainschmitt.perdiction.model.rest.AccountReturnData;
import com.iainschmitt.perdiction.repository.PositionRepository;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.UserService;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PositionRepository positionRepository;

    @GetMapping("/positions")
    public ResponseEntity<List<Position>> getUserPositions(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return new ResponseEntity<>(positionRepository
                .findByUserId(userService.getUserByEmail(authService.getClaim(token, "email")).getId()), HttpStatus.OK);
    }

    @GetMapping("/data")
    public ResponseEntity<AccountReturnData> getUserData(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        var user = userService.getUserByEmail(authService.getClaim(token, "email"));
        return new ResponseEntity<AccountReturnData>(userService.getAccountReturnData(user), HttpStatus.OK);
    }
}
