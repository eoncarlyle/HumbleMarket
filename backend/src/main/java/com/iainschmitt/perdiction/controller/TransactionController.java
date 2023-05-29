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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.iainschmitt.perdiction.exceptions.NotAuthorizedException;
import com.iainschmitt.perdiction.model.rest.TransactionData;
import com.iainschmitt.perdiction.model.rest.TransactionReturnData;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.MarketTransactionService;

@RestController
@RequestMapping("/market")
@CrossOrigin(origins = "*")
@Slf4j
public class TransactionController {
    @Autowired
    private MarketTransactionService transactionService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MarketRepository marketRepository;

    @GetMapping
    public ResponseEntity<List<Market>> getMarkets(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        authService.authenticateTokenThrows(token);
        log.info("Requested markets");
        return new ResponseEntity<>(marketRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{seqId}")
    public ResponseEntity<Market> getMarket(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable int seqId) {
        authService.authenticateTokenThrows(token);
        return new ResponseEntity<>(marketRepository.findBySeqId(seqId), HttpStatus.OK);
    }

    @PostMapping(value = "/{seqId}/outcome/{outcomeIndex}/{positionDirection}/purchase/{shares}")
    public ResponseEntity<TransactionReturnData> purchase(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable int seqId, @PathVariable int outcomeIndex, @PathVariable String positionDirection,
            @PathVariable int shares) {
        authService.authenticateTokenThrows(token);

        return new ResponseEntity<>(transactionService.purchase(authService.getClaim(token, "email"), seqId,
                outcomeIndex, PositionDirection.valueOf(positionDirection), shares), HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/{seqId}/outcome/{outcomeIndex}/{positionDirection}/sale/{shares}")
    public ResponseEntity<TransactionReturnData> sale(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable int seqId, @PathVariable int outcomeIndex, @PathVariable PositionDirection positionDirection,
            @PathVariable int shares) {
        authService.authenticateTokenThrows(token);

        return new ResponseEntity<>(transactionService.sale(authService.getClaim(token, "email"), seqId, outcomeIndex,
                positionDirection, shares), HttpStatus.ACCEPTED);
    }
}
