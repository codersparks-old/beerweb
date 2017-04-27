package uk.codersparks.hackspace.beerweb.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * TODO: Add Javadoc
 */
@Data
@Entity
public class Beer {

    @Id
    private String rfid;

    private int runningTotalRating;

    private int numRatings;

    @Column(unique = true, nullable = true)
    private String pumpId = null;

    private String lastVote = null;
}
