package com.iainschmitt.perdiction.model;

import java.util.List;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Market {
    @Id
    private String id;
    private final String question;
    private final String creatorId;
    private final int marketMakerK;
    private final long closeDate;
    private final List<Outcome> outcomes;
    private final boolean isPublic;
    @Setter
    private boolean isClosed;
    @Setter
    private boolean isResolved;

    // TODO: I recall issues with using builders in repository stored classes, but
    // might be worth looking into
    // TODO: A market lifecycle is Open -> Closed -> Resolved, there are probably
    // better ways to handle this than with the isClosed and isResolved booleans
    public Market(String question, String creatorId, int marketMakerK, long closeDate,
            List<Outcome> outcomes, boolean isPublic, boolean isClosed, boolean isResolved) {
        this.question = question;
        this.creatorId = creatorId;
        this.marketMakerK = marketMakerK;
        this.closeDate = closeDate;
        this.outcomes = outcomes;
        this.isPublic = isPublic;
        this.isClosed = isClosed;
        this.isResolved = isResolved;
    }

    public Outcome getOutcome(int outcomeIndex) {
        return getOutcome(outcomeIndex);
    }
}
