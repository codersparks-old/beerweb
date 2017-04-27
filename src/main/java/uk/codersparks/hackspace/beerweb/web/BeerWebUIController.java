package uk.codersparks.hackspace.beerweb.web;

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

    @RequestMapping(path="/simple")
    public String simple() {
        return "simple";
    }
}
