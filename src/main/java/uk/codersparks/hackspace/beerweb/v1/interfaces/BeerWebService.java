package uk.codersparks.hackspace.beerweb.v1.interfaces;

import uk.codersparks.hackspace.beerweb.v1.model.Beer;

import java.util.Map;

/**
 * TODO: Add Javadoc
 */
public interface BeerWebService {

    void registerBeerRating(String pumpId, String rfid, int rating) throws Exception;

    Map<String, Beer> getAllPumpData() throws Exception;

    void deleteAllBeers() throws Exception;
}
