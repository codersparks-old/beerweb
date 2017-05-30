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
import uk.codersparks.hackspace.beerweb.v2.model.BeerSummary;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;
import uk.codersparks.hackspace.beerweb.v2.model.PumpSummary;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;
import uk.codersparks.hackspace.beerweb.v2.service.DefaultBeerwebService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @RequestMapping(
            path="/",
            method = RequestMethod.DELETE
    )
    public ResponseEntity<Void> deleteAllData() {

        logger.info("Deleting all data...");

        beerwebService.deleteAllData();

        logger.info("...done");

        Map<String, PumpSummary> pumpSummaryMap = this.handleSocketMessage();
        template.convertAndSend("/topic/pumpsummary",pumpSummaryMap);

        return ResponseEntity.noContent().build();
    }


    @GetMapping(
            path = "/ratings",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE}
    )
    public ResponseEntity<List<Rating>> getAllRatings(){

        return ResponseEntity.ok(beerwebService.getRatings());
    }

    @GetMapping(path="/beer", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<Map<String, BeerSummary>> getBeerSummaries() {
        return ResponseEntity.ok(beerwebService.getBeerSummary());
    }

    @GetMapping(path="/beer/{id}",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<BeerSummary> getBeerSummary(@PathVariable(name="id") String id) {
        return ResponseEntity.ok(beerwebService.getBeerSummary(id));
    }

    @GetMapping(path="/beer/ids", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE})
    public ResponseEntity<Set<String>> getBeerIds() {

        Set<String> ids = beerwebService.getBeerIds();

        return ResponseEntity.ok(ids);
    }


    @MessageMapping("/pumpsummary")
    @SendTo("/topic/pumpsummary")
    public Map<String, PumpSummary> handleSocketMessage() {

        logger.info("Received message to handle Socket Message - Generating upto date summary");

        Map<String, PumpSummary> pumpSumaryMap = beerwebService.getPumpSummaryMap();

        logger.info("Sending summary map: {}", pumpSumaryMap);

        return pumpSumaryMap;
    }




}
