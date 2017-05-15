package uk.codersparks.hackspace.beerweb.v1.web;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.codersparks.hackspace.beerweb.v1.interfaces.BeerWebService;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * TODO: Add Javadoc
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class BeerWebControllerTest {

    private MockMvc mockMvc;

    private String pumpId;

    private String rfId;

    @Mock
    private BeerWebService beerWebService;

    @Mock
    private SimpMessagingTemplate template;

    @InjectMocks
    private BeerWebController beerWebController;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        beerWebController =new BeerWebController(template, beerWebService);
        mockMvc = standaloneSetup(beerWebController).build();

        pumpId = "pumpId1";
        rfId = "rfId1";

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test_perform_get() throws Exception {

        mockMvc.perform(get("/")
                .param("pump", pumpId)
                .param("rfid", rfId)
                .param("rating", "5")
        ).andExpect(
          status().isOk()
        );

        verify(beerWebService).registerBeerRating(pumpId, rfId, 5);
        verify(beerWebService).getAllPumpData();
        verify(template).convertAndSend(eq("/topic/pumpdata"),any(Map.class));
        verifyNoMoreInteractions(beerWebService, template);
    }

    @Test
    public void test_perform_post() throws Exception {

        mockMvc.perform(post("/")
                .param("pump", pumpId)
                .param("rfid", rfId)
                .param("rating", "5")
        ).andExpect(
                status().isOk()
        );

        verify(beerWebService).registerBeerRating(pumpId, rfId, 5);
        verify(beerWebService).getAllPumpData();
        verify(template).convertAndSend(eq("/topic/pumpdata"),any(Map.class));
        verifyNoMoreInteractions(beerWebService, template);
    }
}