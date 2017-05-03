package uk.codersparks.hackspace.beerweb.v2.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add Javadoc
 */
@Data
public class PumpSummary {

    private String pumpName;
    private String loadedBeerRfid;
    private int runningTotal = 0;
    private int numRatings = 0;
    private float average = 0;
    private final List<Rating> last10Ratings = new ArrayList<>();
}
