package com.iainschmitt.perdiction.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

import com.iainschmitt.perdiction.model.rest.MarketProposalData;
import com.iainschmitt.perdiction.model.rest.MarketReturnData;
import com.iainschmitt.perdiction.model.rest.MarketTransactionReturnData;
import com.iainschmitt.perdiction.model.rest.PurchaseRequestData;
import com.iainschmitt.perdiction.model.rest.SaleRequestData;
import com.iainschmitt.perdiction.configuration.ExternalisedConfiguration;
import com.iainschmitt.perdiction.model.Market;
import com.iainschmitt.perdiction.model.MarketProposal;
import com.iainschmitt.perdiction.repository.MarketProposalRepository;
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
    private MarketTransactionService marketTransactionService;
    @Autowired
    private AuthService authService;
    @Autowired
    private MarketRepository marketRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private MarketProposalRepository marketProposalRepository;
    @Autowired
    private ExternalisedConfiguration externalConfig;

    @GetMapping
    public ResponseEntity<List<Market>> getMarkets(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        authService.authenticateTokenThrows(token);
        log.info("Requested markets");
        return new ResponseEntity<>(marketRepository.findByIsClosedAndIsResolved(false, false), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketReturnData> getMarket(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @PathVariable String id) {
        authService.authenticateTokenThrows(token);
        var market = marketRepository.findById(id).get();
        var salePriceList = marketTransactionService.getSalePriceList(market,
                userService.getUserByEmail(authService.getClaim(token, "email")));
        return new ResponseEntity<>(MarketReturnData.of(market, salePriceList), HttpStatus.OK);
    }

    @PostMapping(value = "/purchase")
    public ResponseEntity<MarketTransactionReturnData> purchase(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody PurchaseRequestData purchaseRequestData) {
        authService.authenticateTokenThrows(token);
        return new ResponseEntity<>(
                marketTransactionService.purchase(authService.getClaim(token, "email"), purchaseRequestData),
                HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/sale")
    public ResponseEntity<MarketTransactionReturnData> sale(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody SaleRequestData saleRequestData) {
        authService.authenticateTokenThrows(token);
        return new ResponseEntity<>(
                marketTransactionService.sale(authService.getClaim(token, "email"), saleRequestData),
                HttpStatus.ACCEPTED);
    }

    @PostMapping(value = "/market_proposal")
    public ResponseEntity<MarketProposalData> createMarketProposal(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody MarketProposalData marketCreationData) {

        authService.authenticateTokenThrows(token);
        // TODO: Remove admin-only once validation (and rate limiting?) in place
        authService.authenticateAdminThrows(token);
        // TODO: Validation

        marketTransactionService.processMarketProposal(marketCreationData);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "/market_proposal")
    public ResponseEntity<List<MarketProposal>> getMarketProposals(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        authService.authenticateTokenThrows(token);
        log.info("Requested markets");
        return new ResponseEntity<>(marketProposalRepository.findAll(), HttpStatus.OK);
    }

    // TODO: Make this return the id or some other manifestation of the recently
    // accepted market
    @PostMapping(value = "/accept_market_proposal/{marketProposalId}")
    public ResponseEntity<MarketTransactionReturnData> acceptMarketProposal(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable String marketProposalId) {
        authService.authenticateTokenThrows(token);
        authService.authenticateAdminThrows(token);
        return new ResponseEntity<>(marketTransactionService.acceptMarketProposal(marketProposalId),
                HttpStatus.ACCEPTED);
    }

    // TODO: Make this return the id or some other manifestation of the recently
    // accepted market
    @PostMapping(value = "/reject_market_proposal/{marketProposalId}")
    public ResponseEntity<MarketTransactionReturnData> rejectMarketProposal(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token, @PathVariable String marketProposalId) {

        authService.authenticateTokenThrows(token);
        authService.authenticateAdminThrows(token);
        return new ResponseEntity<MarketTransactionReturnData>(
                marketTransactionService.rejectMarketProposal(marketProposalId), null, HttpStatus.ACCEPTED);
    }

}
