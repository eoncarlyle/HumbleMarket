package com.iainschmitt.perdiction.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.TransactionService;

@RestController
@RequestMapping("/market")
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MarketRepository marketRepository;

    @GetMapping
    public ResponseEntity<List<Market>> getMarkets(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        authService.authenticateToken(token);
        return new ResponseEntity<>(marketRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{seqId}")
    public ResponseEntity<Market> getMarket(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable int seqId) {
        authService.authenticateToken(token);
        return new ResponseEntity<>(marketRepository.findBySeqId(seqId), HttpStatus.OK);
    }

}
