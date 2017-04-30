package uk.codersparks.hackspace.beerweb.v2.web;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebException;
import uk.codersparks.hackspace.beerweb.v2.interfaces.BeerwebService;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * TODO: Add Javadoc
 */
@RestController
@RequestMapping(path = "/v2/api/")
public class BeerwebRestController {

    private static final Logger logger = LoggerFactory.getLogger(BeerwebRestController.class);

    private final BeerwebService beerwebService;
    private final SimpMessagingTemplate template;

    public BeerwebRestController(BeerwebService beerwebService, SimpMessagingTemplate template) {
        this.beerwebService = beerwebService;
        this.template = template;
    }


    @RequestMapping(
            path = "/pump/{id}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
            )
    public ResponseEntity<Pump> addPump(@PathVariable("id") String pumpName) {

        Pump pump = beerwebService.createNewPump(pumpName);

        Map<String, PumpSummary> pumpSummaryMap = this.handleSocketMessage();
        template.convertAndSend("/topic/pumpsummary",pumpSummaryMap);

        return ResponseEntity.ok(pump);
    }

    @RequestMapping(
            path="/pump/{id}",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> deletePump(@PathVariable("id") String pumpName) throws BeerwebException {

        if(pumpName == null || pumpName.length() < 1 ) {
            String error = "Pump name must not be null or empty, supplied: " + pumpName;
            logger.error(error);
            throw new BeerwebException(error);
        }

        beerwebService.deletePump(pumpName);

        Map<String, PumpSummary> pumpSummaryMap = this.handleSocketMessage();
        template.convertAndSend("/topic/pumpsummary",pumpSummaryMap);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(
            path="/pump/loaded",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    public ResponseEntity<List<Pump>> getLoadedPumps() {

        List<Pump> pumpList = beerwebService.getAllPumps().stream().filter(pump -> pump.getAssignedRfid() != null).collect(Collectors.toList());

        return ResponseEntity.ok(pumpList);
    }

    @RequestMapping(
            path = "/pump/{id}/beer/{rfid}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    public ResponseEntity<Pump> loadBeerToPump(@PathVariable("rfid") String beerRfid, @PathVariable("id") String pumpName ) throws BeerwebException {

        if(pumpName == null || pumpName.length() < 1 || beerRfid ==null || beerRfid.length() < 1) {
            String error = "Pump name and Beer rfid must not be null or empty, supplied: BeerRFID: " + beerRfid + " pump name: " + pumpName;
            logger.error(error);
            throw new BeerwebException(error);
        }

        Pump pump = beerwebService.loadBeerToPump(beerRfid, pumpName);

        Map<String, PumpSummary> pumpSummaryMap = this.handleSocketMessage();
        template.convertAndSend("/topic/pumpsummary",pumpSummaryMap);

        return ResponseEntity.ok(pump);
    }

    @RequestMapping(
            path="/pump/{id}/rating/{score}",
            method=RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    public ResponseEntity<Rating> registerRating(@PathVariable("id")String pumpName, @PathVariable("score") int score) throws BeerwebException {

        Rating rating = beerwebService.registerRating(pumpName, score);

        Map<String, PumpSummary> pumpSummaryMap = this.handleSocketMessage();
        template.convertAndSend("/topic/pumpsummary",pumpSummaryMap);

        return ResponseEntity.ok(rating);
    }

    @GetMapping(
            path = "/ratings",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    public ResponseEntity<List<Rating>> getAllRatings(){

        return ResponseEntity.ok(beerwebService.getRatings());
    }


    @MessageMapping("/pumpsummary")
    @SendTo("/topic/pumpsummary")
    public Map<String, PumpSummary> handleSocketMessage() {

        logger.info("Received message to handle Socket Message - Generating upto date summary");

        Map<String, PumpSummary> pumpSumaryMap = StreamSupport.stream(beerwebService.getAllPumps().spliterator(), false)
                .filter(pump -> pump.getAssignedRfid() != null)
                .map(pump -> {
                    PumpSummary pumpSummary = new PumpSummary();
                    pumpSummary.setPumpName(pump.getPumpName());
                    pumpSummary.setLoadedBeerRfid(pump.getAssignedRfid());

                    Iterable<Rating> ratings = beerwebService.getRatingsByBeer(pump.getAssignedRfid());

                    int numRatings = 0;
                    int runningTotal = 0;

                    // Can't think of a way to do this with streams yet
                    for(Rating rating : ratings) {

                        numRatings += 1;
                        runningTotal += rating.getRating();

                        // Would normally do this in a stream but we are looping over each item already

                        pumpSummary.getRatings().add(rating);
                    }



                    pumpSummary.setNumRatings(numRatings);
                    pumpSummary.setRunningTotal(runningTotal);
                    // If the numRatings is 0 then average is zero otherwise calculate
                    pumpSummary.setAverage(numRatings == 0 ? 0 : (float)runningTotal/numRatings);


                    return pumpSummary;

                })
                .collect(Collectors.toMap(PumpSummary::getPumpName, Function.identity()));

        logger.info("Sending summary map: {}", pumpSumaryMap);

        return pumpSumaryMap;
    }

    @Data
    public static final class PumpSummary {

        private String pumpName;
        private String loadedBeerRfid;
        private int runningTotal = 0;
        private int numRatings = 0;
        private float average = 0;
        private final List<Rating> ratings = new ArrayList<>();



    }


}
