package uk.codersparks.hackspace.beerweb.v2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebException;
import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebNotFoundException;
import uk.codersparks.hackspace.beerweb.v2.interfaces.BeerwebService;
import uk.codersparks.hackspace.beerweb.v2.model.BeerSummary;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;
import uk.codersparks.hackspace.beerweb.v2.model.PumpSummary;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;
import uk.codersparks.hackspace.beerweb.v2.repository.PumpRepository;
import uk.codersparks.hackspace.beerweb.v2.repository.RatingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO: Add Javadoc
 */
@Service
public class DefaultBeerwebService implements BeerwebService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBeerwebService.class);

    private final PumpRepository pumpRepository;
    private final RatingRepository ratingRepository;

    public DefaultBeerwebService(PumpRepository pumpRepository, RatingRepository ratingRepository) {
        this.pumpRepository = pumpRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    public Pump createNewPump(String pumpName) {

        Pump pump = pumpRepository.findByPumpName(pumpName);
        logger.info("Pump from repository: {}", pump);
        if(pump != null) {
            logger.info("Pump already exists with pumpName: {} therefore not creating a new one", pumpName);
        } else {
            logger.debug("Creating pump with pumpName: {}", pumpName);
            pump = new Pump(pumpName);
            logger.info("Saving new pump...");
            pump = pumpRepository.save(pump);
            logger.info("...done");
        }
        logger.debug("Returning pump: {}", pump);
        return pump;
    }

    @Override
    public void deletePump(Pump pump) throws BeerwebException {

        if( ! pumpRepository.exists(pump.getId())) {
            throw new BeerwebNotFoundException("Cannot find Pump with id: " + pump.getId());
        }
        logger.info("Deleting pump: {}", pump);
        pumpRepository.delete(pump);
    }

    @Override
    public void deletePump(String pumpName) throws BeerwebException{

        Pump pump = pumpRepository.findByPumpName(pumpName);

        if(pump == null) {
            throw new BeerwebNotFoundException("Cannot find pump with name: " + pumpName);
        }

        this.deletePump(pump);
    }

    @Override
    public void deleteAllData() {

        ratingRepository.deleteAll();
        pumpRepository.deleteAll();

    }


    @Override
    public List<Pump> getAllPumps() {
        Iterable<Pump> pumpIterable = pumpRepository.findAll();

        List<Pump> pumpList = StreamSupport.stream(pumpIterable.spliterator(), true).collect(Collectors.toList());

        return pumpList;
    }

    @Override
    public Pump loadBeerToPump(String beerRfid, String pumpName) throws BeerwebException{

        Pump pump = pumpRepository.findByPumpName(pumpName);

        if( pump == null) {
            logger.warn("No pump found with name: {} creating it now", pumpName);
            pump = new Pump(pumpName);
        }

        logger.info("Assigning beer rfid {} to pump {}", beerRfid, pumpName);
        pump.setAssignedRfid(beerRfid);

        logger.debug("Saving updated pump...");
        pump = pumpRepository.save(pump);
        logger.debug("...done");

        return pump;

    }

    @Override
    public Rating registerRating(String pumpName, int score) throws BeerwebException {

        Pump pump = pumpRepository.findByPumpName(pumpName);

        if(pump == null) {
            String error = "No pump found with name: " + pumpName;
            logger.error(error);
            throw new BeerwebNotFoundException(error);
        }

        if(pump.getAssignedRfid() == null) {
            String error = "No beer loaded into pump: " + pumpName;
            logger.error(error);
            throw new BeerwebNotFoundException(error);
        }

        Rating rating = new Rating();
        rating.setRfid(pump.getAssignedRfid());
        rating.setRating(score);
        rating.setTimestamp(LocalDateTime.now());

        logger.debug("Saving rating {}...", rating);
        rating = ratingRepository.save(rating);
        logger.debug("...done");

        return rating;
    }

    @Override
    public List<Rating> getRatings() {
        List<Rating> ratings = StreamSupport.stream(ratingRepository.findAll().spliterator(), false).collect(Collectors.toList());
        return ratings;
    }

    @Override
    public List<Rating> getRatingsByBeer(String rfid) {

        List<Rating> ratings = StreamSupport.stream(ratingRepository.findAllByRfidOrderByTimestampDesc(rfid).spliterator(), false).collect(Collectors.toList());

        return ratings;
    }

    @Override
    public Map<String, PumpSummary> getPumpSummaryMap() {
        return StreamSupport.stream(this.getAllPumps().spliterator(), false)
                .filter(pump -> pump.getAssignedRfid() != null)
                .map(pump -> {
                    PumpSummary pumpSummary = new PumpSummary();
                    pumpSummary.setPumpName(pump.getPumpName());
                    pumpSummary.setLoadedBeerRfid(pump.getAssignedRfid());

                    Iterable<Rating> ratings = this.getRatingsByBeer(pump.getAssignedRfid());

                    int numRatings = 0;
                    int runningTotal = 0;

                    // Can't think of a way to do this with streams yet
                    for(Rating rating : ratings) {

                        numRatings += 1;
                        runningTotal += rating.getRating();

                        // Would normally do this in a stream but we are looping over each item already
                        if(pumpSummary.getLast10Ratings().size() < 10) {
                            pumpSummary.getLast10Ratings().add(rating);
                        }
                    }



                    pumpSummary.setNumRatings(numRatings);
                    pumpSummary.setRunningTotal(runningTotal);
                    // If the numRatings is 0 then average is zero otherwise calculate
                    pumpSummary.setAverage(numRatings == 0 ? 0 : (float)runningTotal/numRatings);


                    return pumpSummary;

                })
                .collect(Collectors.toMap(PumpSummary::getPumpName, Function.identity()));
    }

    @Override
    public Map<String, BeerSummary> getBeerSummary() {


        final Map<String, List<Rating>> ratingsByRfid = StreamSupport.stream(ratingRepository.findAll().spliterator(), false).collect(Collectors.groupingBy(Rating::getRfid));

        Map<String, BeerSummary> beerSummaryMap = ratingsByRfid.keySet().stream().parallel().map(rfid -> {

            BeerSummary beerSummary = new BeerSummary();

            List<Rating> ratings = ratingsByRfid.get(rfid);
            beerSummary.setBeerRfId(rfid);

            double averageRating = ratings.stream().mapToInt(Rating::getRating).average().getAsDouble();
            beerSummary.setAverageRatings(averageRating);

            beerSummary.setNumberOfRatings(ratings.size());

            beerSummary.setRatings(ratings);

            return beerSummary;
        }).collect(Collectors.toMap(BeerSummary::getBeerRfId, Function.identity()));

        return beerSummaryMap;

    }


    @Override
    public BeerSummary getBeerSummary(String id) {

        Iterable<Rating> ratings = ratingRepository.findAllByRfidOrderByTimestampDesc(id);

        BeerSummary beerSummary = new BeerSummary();
        beerSummary.setBeerRfId(id);

        StreamSupport.stream(ratings.spliterator(), true).forEach(rating -> beerSummary.getRatings().add(rating));

        double averageRating = StreamSupport.stream(ratings.spliterator(), false).mapToInt(Rating::getRating).average().getAsDouble();
        beerSummary.setAverageRatings(averageRating);
        beerSummary.setNumberOfRatings(beerSummary.getRatings().size());

        return beerSummary;
    }

    @Override
    public Set<String> getBeerIds() {
        Iterable<Rating> ratings = ratingRepository.findAll();

        final Map<String, List<Rating>> ratingsByRfid = StreamSupport.stream(ratingRepository.findAll().spliterator(), false).collect(Collectors.groupingBy(Rating::getRfid));

        return ratingsByRfid.keySet();
    }


}