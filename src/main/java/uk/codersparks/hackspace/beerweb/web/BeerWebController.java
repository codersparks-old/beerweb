package uk.codersparks.hackspace.beerweb.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import uk.codersparks.hackspace.beerweb.interfaces.BeerWebService;
import uk.codersparks.hackspace.beerweb.model.Beer;

import java.util.Map;

/**
 * TODO: Add Javadoc
 */
@RestController
public class BeerWebController {

    private static final Logger logger = LoggerFactory.getLogger(BeerWebController.class);

    private final SimpMessagingTemplate template;

    private final BeerWebService beerWebService;

    public BeerWebController(SimpMessagingTemplate template, BeerWebService beerWebService) {
        this.template = template;
        this.beerWebService = beerWebService;
    }

    @RequestMapping(path="/", method= RequestMethod.POST)
    public ResponseEntity<Void> addRating(@RequestParam("pump") String pumpId, @RequestParam("rfid") String rfid, @RequestParam("rating") int rating) throws Exception {

        logger.info("Received GET rating: {} for rfid: {} on pump: {}", rating, rfid, pumpId);

        beerWebService.registerBeerRating(pumpId, rfid, rating);

        // Now we get the pump data to send to the websocket
        Map<String, Beer> pumpMap = beerWebService.getAllPumpData();

        template.convertAndSend("/topic/pumpdata",pumpMap);

        return ResponseEntity.ok().build();
    }

    // Hacky way of including forth optional param to make method signature not identical as we want the user to be able to register using either get or post
    @GetMapping(path="/")
    public ResponseEntity<Void> addRating(@RequestParam("pump") String pumpId, @RequestParam("rfid") String rfid, @RequestParam("rating") int rating, @RequestParam(value = "none", defaultValue = "none", required = false) String none) throws Exception {

        logger.info("Received POST rating: {} for rfid: {} on pump: {}", rating, rfid, pumpId);

        return this.addRating(pumpId, rfid, rating);
    }

    @RequestMapping(path="/danger/deleteall", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteAllBeers() throws Exception {

        logger.info("Received DELETE request for all beers");

        beerWebService.deleteAllBeers();

        Map<String, Beer> pumpMap = beerWebService.getAllPumpData();

        template.convertAndSend("/topic/pumpdata",pumpMap);

        return ResponseEntity.ok().build();

    }

    // We are going to serve the pump data via websocket
    @MessageMapping("/pumpdata")
    @SendTo("/topic/pumpdata")
    public Map<String, Beer> getPumpData() throws Exception {

        return beerWebService.getAllPumpData();

    }
}
