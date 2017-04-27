package uk.codersparks.hackspace.beerweb.interfaces;

import uk.codersparks.hackspace.beerweb.exception.BeerWebException;
import uk.codersparks.hackspace.beerweb.model.Beer;

import java.util.Map;

/**
 * TODO: Add Javadoc
 */
public interface BeerWebService {

    void registerBeerRating(String pumpId, String rfid, int rating) throws Exception;

    Map<String, Beer> getAllPumpData() throws Exception;

    void deleteAllBeers() throws Exception;
}
