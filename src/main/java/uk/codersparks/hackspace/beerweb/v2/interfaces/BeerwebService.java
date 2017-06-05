package uk.codersparks.hackspace.beerweb.v2.interfaces;

import uk.codersparks.hackspace.beerweb.v1.exception.BeerWebException;
import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebException;
import uk.codersparks.hackspace.beerweb.v2.model.BeerSummary;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;
import uk.codersparks.hackspace.beerweb.v2.model.PumpSummary;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Add Javadoc
 */
public interface BeerwebService {

    Pump createNewPump(String pumpName);

    void deletePump(Pump pump) throws BeerwebException;

    void deletePump(String pump) throws BeerwebException;

    List<Pump> getAllPumps();

    void deleteAllData();

    Pump loadBeerToPump(String beerRfid, String pumpName) throws BeerwebException;

    Pump savePumpBeerName(String pumpName, String beerName) throws BeerwebException;

    Rating registerRating(String pumpName, int rating) throws BeerwebException;

    List<Rating> getRatings();

    List<Rating> getRatingsByBeer(String rfid);

    Map<String, PumpSummary> getPumpSummaryMap();

    Map<String, BeerSummary> getBeerSummary();

    BeerSummary getBeerSummary(String id);

    Set<String> getBeerIds();
}
