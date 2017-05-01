package uk.codersparks.hackspace.beerweb.v1.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * TODO: Add Javadoc
 */
@Controller
@RequestMapping(path = "/ui")
public class BeerWebUIController {

    @RequestMapping(path="/index.html")
    public String index() {
        return "index";
    }

    @RequestMapping(path="/manual.html")
    public String manual() {
        return "manual";
    }
}
