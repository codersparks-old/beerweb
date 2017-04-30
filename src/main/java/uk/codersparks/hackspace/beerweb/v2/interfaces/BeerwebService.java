package uk.codersparks.hackspace.beerweb.v2.interfaces;

import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebException;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;

import java.util.List;

/**
 * TODO: Add Javadoc
 */
public interface BeerwebService {

    Pump createNewPump(String pumpName);

    void deletePump(Pump pump) throws BeerwebException;

    void deletePump(String pump) throws BeerwebException;

    List<Pump> getAllPumps();

    Pump loadBeerToPump(String beerRfid, String pumpName) throws BeerwebException;

    Rating registerRating(String pumpName, int rating) throws BeerwebException;

    List<Rating> getRatings();

    List<Rating> getRatingsByBeer(String rfid);

}
