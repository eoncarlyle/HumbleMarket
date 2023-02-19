package com.iainschmitt.perdiction.model;

import java.util.Set;

/*
 * At a single price point there can be multiple transaction proposals,
 * As if a user wants 10 shares at 0.40 CR where userA and userB both
 * put 6 shares up for sale, the seller that put thier shares up will be
 * bought out first
 */
//TODO: Make sure the sale FIFO logic works
//TODO: enforce that a transaction proposal can only be associated with one user
public class TransactionProposal {
    // This is an aggregate position rathe than a true position, as
    // the proposed sale might composed of multiple positions bought at
    // different times that are represented as one
    // However, a transaction proposal will only corrospond to one user
    private Set<String> positionIds;
    private CreditValue proposedPrice;
}
