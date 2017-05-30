package uk.codersparks.hackspace.beerweb.v2.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Javadoc
 */
@Data
public class BeerSummary {

    private String beerRfId;
    private int numberOfRatings;
    private double averageRatings;
    private final List<Rating> ratings = new ArrayList<>();


    public void setRatings(List<Rating> ratings) {
        this.ratings.clear();
        this.ratings.addAll(ratings);
    }
}
