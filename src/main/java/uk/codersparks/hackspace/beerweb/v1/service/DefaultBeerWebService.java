package uk.codersparks.hackspace.beerweb.v1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.codersparks.hackspace.beerweb.v1.exception.BeerWebException;
import uk.codersparks.hackspace.beerweb.v1.interfaces.BeerWebService;
import uk.codersparks.hackspace.beerweb.v1.model.Beer;
import uk.codersparks.hackspace.beerweb.v1.repository.BeerRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO: Add Javadoc
 */
@Service
public class DefaultBeerWebService implements BeerWebService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBeerWebService.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final BeerRepository beerRepository;

    public DefaultBeerWebService(BeerRepository beerRepository) {
        this.beerRepository = beerRepository;
    }

    @Override
    public void registerBeerRating(String pumpId, String rfid, int rating) throws Exception {

        LocalDateTime thisVoteDate = LocalDateTime.now(ZoneId.of("Europe/London"));
        String thisVoteDateString = DATE_FORMATTER.format(thisVoteDate);

        if(rating == 0) {

            logger.info("Rating detected as zero, assuming new barrel loaded, rfid: {}, pump: {}", rfid, pumpId);

            if(beerRepository.exists(rfid)) {

                // TODO: Is this right should it just reasign the beer to the pump as it may be a keg is being reloaded
                logger.error("Duplicate rfid found, don't know what to do so I am cowardly ignoring");
                return;
            }

            Beer beer = beerRepository.findByPumpId(pumpId);

            if(beer != null) {
                logger.info("Found beer with rfid {} on pump {} deregistering...", beer.getRfid(), pumpId);
                beer.setPumpId(null);
                beerRepository.save(beer);
                logger.info("...done");
            }

            beer = new Beer();

            beer.setPumpId(pumpId);
            beer.setRfid(rfid);
            beer.setNumRatings(0);
            beer.setRunningTotalRating(rating);


            beer.setLastVote(thisVoteDateString);

            logger.info("Created new Beer: {} and saving to db...", beer);
            beerRepository.save(beer);
            logger.info("...done");

        } else if(rating <= 5) {
            logger.info("Registering rating of {} for beer {} on pump {}", rating, rfid, pumpId);

            Beer beer = beerRepository.findOne(rfid);

            if(beer == null) {
                // TODO:  Should this be the case or should it actually create the new beer ????
                throw new BeerWebException("Cannot find beer with rfid: " + rfid);
            }
            beer.setRunningTotalRating(beer.getRunningTotalRating() + rating);
            beer.setNumRatings(beer.getNumRatings() + 1);
            beer.setLastVote(thisVoteDateString);

            logger.info("Saving updated beer: {}...", beer);
            beerRepository.save(beer);
            logger.info("...done");
        } else {
            throw new BeerWebException("Rating can only be 0..5, supplied: " + rating);
        }

    }

    @Override
    public Map<String, Beer> getAllPumpData() throws Exception{

        logger.info("Getting pump data from repository");

        Collection<Beer> beerList = beerRepository.findByPumpIdIsNotNull();

        logger.info("Beer List returned: {}", beerList);

        Map<String, Beer> pumpMap;

        if(beerList == null) {
            pumpMap = Collections.emptyMap();
        } else {
            pumpMap = StreamSupport.stream(beerList.spliterator(), true).collect(Collectors.toMap(Beer::getPumpId, beer -> beer));
        }

        return pumpMap;

    }

    @Override
    public void deleteAllBeers() throws Exception {

        logger.warn("Delete All Beers functionallity called");

        beerRepository.delete(beerRepository.findAll());
    }
}
