package com.iainschmitt.perdiction.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import com.iainschmitt.perdiction.model.rest.MarketReturnData;
import com.iainschmitt.perdiction.model.rest.TransactionReturnData;
import com.iainschmitt.perdiction.model.rest.PurchaseRequestData;
import com.iainschmitt.perdiction.model.rest.SaleRequestData;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.PositionDirection;
import com.iainschmitt.perdiction.repository.MarketRepository;
import com.iainschmitt.perdiction.service.AuthService;
import com.iainschmitt.perdiction.service.MarketTransactionService;
import com.iainschmitt.perdiction.service.UserService;

@RestController
@RequestMapping("/market")
@CrossOrigin(origins = "*")
@Slf4j
public class MarketTransactionController {
    @Autowired
    private MarketTransactionService transactionService;

    @Autowired
    private AuthService authService;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Market>> getMarkets(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        authService.authenticateTokenThrows(token);
        log.info("Requested markets");
        return new ResponseEntity<>(marketRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{seqId}")
    public ResponseEntity<MarketReturnData> getMarket(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable int seqId) {
        authService.authenticateTokenThrows(token);
        var market = marketRepository.findBySeqId(seqId);
        var salePriceList = transactionService.getSalePriceList(market,
                userService.getUserByEmail(authService.getClaim(token, "email")));
        return new ResponseEntity<>(MarketReturnData.of(market, salePriceList), HttpStatus.OK);
    }

    @PostMapping(value = "/purchase")
    public ResponseEntity<TransactionReturnData> purchase(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody PurchaseRequestData purchaseRequestData) {
        authService.authenticateTokenThrows(token);
        return new ResponseEntity<>(
                transactionService.purchase(authService.getClaim(token, "email"), purchaseRequestData),
                HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/sale")
    public ResponseEntity<TransactionReturnData> sale(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody SaleRequestData saleRequestData) {
        authService.authenticateTokenThrows(token);
        return new ResponseEntity<TransactionReturnData>(transactionService.sale(authService.getClaim(token, "email"), saleRequestData),
                HttpStatus.ACCEPTED);
    }
}
