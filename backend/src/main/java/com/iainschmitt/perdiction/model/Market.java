package com.iainschmitt.perdiction.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.data.annotation.Id;

public class Market {
    @Id
    private String id;
    private final String question;
    private final String creatorId;
    private final long resolveDate;
    private final List<Outcome> outcomes;
    private final Map<Outcome, List<TransactionProposal>> orderBook;
    private boolean isPublic;

    // TODO: Implement market opening logic,
    public Market(String question, String creatorId, long resolveDate, List<Outcome> outcomes, boolean isPublic) {
        this.question = question;
        this.creatorId = creatorId;
        this.outcomes = outcomes;
        this.resolveDate = resolveDate;
        this.orderBook = new HashMap<Outcome, List<TransactionProposal>>() {
            {
                outcomes.forEach(outcome -> put(outcome, new ArrayList<>()));
            }
        };
    }
}
