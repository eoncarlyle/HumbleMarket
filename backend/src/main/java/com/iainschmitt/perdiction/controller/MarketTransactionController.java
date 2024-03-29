package com.iainschmitt.prediction.controller;

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

import com.iainschmitt.prediction.model.Market;
import com.iainschmitt.prediction.model.MarketProposal;
import com.iainschmitt.prediction.model.MarketTransaction;
import com.iainschmitt.prediction.model.PositionDirection;
import com.iainschmitt.prediction.model.rest.MarketProposalData;
import com.iainschmitt.prediction.model.rest.MarketReturnData;
import com.iainschmitt.prediction.model.rest.MarketTransactionRequestData;
import com.iainschmitt.prediction.repository.MarketProposalRepository;
import com.iainschmitt.prediction.repository.MarketRepository;
import com.iainschmitt.prediction.service.AuthService;
import com.iainschmitt.prediction.service.MarketTransactionService;
import com.iainschmitt.prediction.service.UserService;

import lombok.extern.slf4j.Slf4j;

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

        @GetMapping
        public ResponseEntity<List<Market>> getMarkets(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
                authService.authenticateTokenThrows(token);
                log.info("Requested markets");
                return new ResponseEntity<>(
                                marketRepository.findByIsClosedAndIsResolved(false, false),
                                HttpStatus.OK);
        }

        @GetMapping("/{id}")
        public ResponseEntity<MarketReturnData> getMarket(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @PathVariable String id) {
                authService.authenticateTokenThrows(token);
                var user = userService.getUserByEmail(authService.getClaim(token, "email"));
                var market = marketRepository.findById(id).get();
                var userCredits = user.getCredits();
                return new ResponseEntity<>(
                                MarketReturnData.of(market, marketTransactionService.getPurchasePriceList(market, user),
                                                marketTransactionService.getSalePriceList(market, user), userCredits),
                                HttpStatus.OK);
        }

        @PostMapping(value = "/purchase")
        public ResponseEntity<MarketTransaction> purchase(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @RequestBody MarketTransactionRequestData purchaseRequestData) {
                authService.authenticateTokenThrows(token);
                return new ResponseEntity<>(
                                marketTransactionService.purchase(authService.getClaim(token, "email"),
                                                purchaseRequestData),
                                HttpStatus.ACCEPTED);
        }

        @PostMapping(value = "/sale")
        public ResponseEntity<MarketTransaction> sale(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @RequestBody MarketTransactionRequestData marketTransactionRequestData) {
                authService.authenticateTokenThrows(token);
                return new ResponseEntity<>(
                                marketTransactionService.sale(
                                                authService.getClaim(token, "email"),
                                                marketTransactionRequestData),
                                HttpStatus.ACCEPTED);
        }

        // TODO: Change this to put
        @PostMapping(value = "/market_proposal")
        public ResponseEntity<MarketProposal> createMarketProposal(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @RequestBody MarketProposalData marketProposalData) {
                authService.authenticateTokenThrows(token);
                // TODO: Remove admin-only once validation (and rate limiting?) in place
                authService.authenticateAdminThrows(token);
                // TODO: Validation

                return new ResponseEntity<>(
                                marketTransactionService.processMarketProposal(marketProposalData),
                                HttpStatus.ACCEPTED);
        }

        @GetMapping(value = "/market_proposal")
        public ResponseEntity<List<MarketProposal>> getMarketProposals(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
                authService.authenticateTokenThrows(token);
                // log.info("Requested markets");
                return new ResponseEntity<>(
                                marketProposalRepository.findAll(),
                                HttpStatus.OK);
        }

        // TODO: Change this to put
        @PostMapping(value = "/accept_market_proposal/{marketProposalId}")
        public ResponseEntity<MarketProposal> acceptMarketProposal(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @PathVariable String marketProposalId) {
                authService.authenticateTokenThrows(token);
                authService.authenticateAdminThrows(token);
                return new ResponseEntity<>(
                                marketTransactionService.acceptMarketProposal(marketProposalId),
                                HttpStatus.ACCEPTED);
        }

        @PostMapping(value = "/reject_market_proposal/{marketProposalId}")
        public ResponseEntity<MarketProposal> rejectMarketProposal(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @PathVariable String marketProposalId) {
                authService.authenticateTokenThrows(token);
                authService.authenticateAdminThrows(token);
                return new ResponseEntity<>(
                                marketTransactionService.rejectMarketProposal(marketProposalId),
                                null,
                                HttpStatus.ACCEPTED);
        }

        @GetMapping(value = "/resolved")
        public ResponseEntity<List<Market>> getMarketsReadyForResolution(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
                authService.authenticateTokenThrows(token);
                authService.authenticateAdminThrows(token);
                return new ResponseEntity<>(
                                marketRepository.findByIsClosedAndIsResolved(true, false),
                                HttpStatus.OK);
        }

        @PostMapping(value = "/resolve_market/{marketId}/{outcomeIndex}")
        public ResponseEntity<Market> resolveMarket(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @PathVariable String marketId,
                        @PathVariable int outcomeIndex) {
                var market = marketRepository.findById(marketId).get();
                return new ResponseEntity<>(
                                marketTransactionService.resolve(
                                                market,
                                                outcomeIndex,
                                                PositionDirection.YES),
                                HttpStatus.ACCEPTED);
        }

        @PostMapping(value = "/resolve_market/{marketId}/direction/{positionDirection}")
        public ResponseEntity<Market> resolveSingleOutcomeMarket(
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @PathVariable String marketId,
                        @PathVariable PositionDirection positionDirection) {
                var market = marketRepository.findById(marketId).get();
                if (market.getOutcomes().size() > 1) {
                        throw new IllegalArgumentException(
                                        String.format(
                                                        "Cannot use single outcome resolution endpoint for market with '%d' endpoints",
                                                        market.getOutcomes().size()));
                }

                return new ResponseEntity<>(
                                marketTransactionService.resolve(market, 0, positionDirection),
                                HttpStatus.ACCEPTED);
        }
}
