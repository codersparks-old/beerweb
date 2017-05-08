package uk.codersparks.hackspace.beerweb.v2.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * TODO: Add Javadoc
 */
@Controller
public class BeerwebUIController {

    @GetMapping(path = "/v2/index.html")
    public String index() {
        return "v2/index";
    }

    @GetMapping(path = "/v2/manual.html")
    public String manual() {
        return "v2/manual";
    }

    @GetMapping(path="/v2/d3.html")
    public  String d3() { return "v2/d3";}
}
