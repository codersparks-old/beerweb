package uk.codersparks.hackspace.beerweb.v2.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebException;
import uk.codersparks.hackspace.beerweb.v2.exception.BeerwebNotFoundException;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;
import uk.codersparks.hackspace.beerweb.v2.repository.PumpRepository;
import uk.codersparks.hackspace.beerweb.v2.repository.RatingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * TODO: Add Javadoc
 */
public class DefaultBeerwebServiceTest {

    @Mock
    private PumpRepository mockPumpRepository;

    @Mock
    private RatingRepository mockRatingRepository;

    @InjectMocks
    private DefaultBeerwebService underTest;

    @Captor
    private ArgumentCaptor<Pump> pumpCaptor;

    @Captor
    private ArgumentCaptor<Rating> ratingCaptor;

    private String[] pumps = {"p1","p2","p3","p4"};

    private Rating[] ratings = {
            new Rating("12345", 3, LocalDateTime.now()),
            new Rating("54321", 4, LocalDateTime.now()),
            new Rating("33333", 3, LocalDateTime.now()),
    };

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void createNewPump_notExisting() throws Exception {

        String pumpName = pumps[0];

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(null);

        underTest.createNewPump(pumpName);

        verify(mockPumpRepository).findByPumpName(pumpName);
        verify(mockPumpRepository).save(pumpCaptor.capture());
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

        Pump pump = pumpCaptor.getValue();

        assertEquals(pumpName, pump.getPumpName());
        assertNull(pump.getAssignedRfid());

    }

    @Test
    public void createNewPump_existing() throws Exception {

        String pumpName = pumps[0];

        Pump pump = new Pump(pumpName);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(pump);

        underTest.createNewPump(pumpName);

        verify(mockPumpRepository).findByPumpName(pumpName);
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

    }

    @Test
    public void deletePump_notExisting() {

        long id = 1l;

        Pump pump = new Pump(pumps[0]);
        pump.setId(id);

        when(mockPumpRepository.exists(id)).thenReturn(false);


        try {
            underTest.deletePump(pump);
            fail("Expected BeerwebNotFoundException expected");
        } catch (BeerwebException e) {
            verify(mockPumpRepository).exists(id);
            verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);
        }


    }

    @Test
    public void deletePump_existing() throws Exception {

        long id = 1l;

        Pump pump = new Pump(pumps[0]);
        pump.setId(id);

        when(mockPumpRepository.exists(id)).thenReturn(true);

        underTest.deletePump(pump);
        verify(mockPumpRepository).exists(id);
        verify(mockPumpRepository).delete(pump);
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

    }

    @Test
    public void deletePumpByName_notExistingId(){

        String pumpName = pumps[0];

        Pump pump = new Pump(pumpName);
        pump.setId(1);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(pump);
        when(mockPumpRepository.exists(anyLong())).thenReturn(false);

        try {
            underTest.deletePump(pumpName);
            fail("BeerwebNotFoundException expected");
        } catch (BeerwebException e) {
            verify(mockPumpRepository).findByPumpName(pumpName);
            verify(mockPumpRepository).exists(anyLong());
            verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);
        }
    }

    @Test
    public void deletePumpByName_notExistingPumpName(){

        String pumpName = pumps[0];

        Pump pump = new Pump(pumpName);
        pump.setId(1);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(null);

        try {
            underTest.deletePump(pumpName);
            fail("BeerwebNotFoundException expected");
        } catch (BeerwebException e) {
            verify(mockPumpRepository).findByPumpName(pumpName);
            verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);
        }
    }

    @Test
    public void deleteAllData() throws Exception {

        underTest.deleteAllData();

        verify(mockPumpRepository).deleteAll();
        verify(mockRatingRepository).deleteAll();
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);
    }

    @Test
    public void getAllPumps() throws Exception {

        List<Pump> pumpList = Arrays.stream(pumps).map(pumpName -> {
            Pump pump = new Pump(pumpName);
            pump.setId(pumpName.hashCode());
            return pump;
        }).collect(Collectors.toList());

        when(mockPumpRepository.findAll()).thenReturn(pumpList);

        List<Pump> actualList = underTest.getAllPumps();

        verify(mockPumpRepository).findAll();
        verifyNoMoreInteractions(mockPumpRepository,mockRatingRepository);

        assertEquals(pumpList, actualList);

    }

    @Test
    public void loadBeerToPump_pumpExisting() throws Exception {

        String beerRFID = "12345";

        String pumpName = pumps[0];
        Pump pump = new Pump(pumpName);
        pump.setId(1);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(pump);

        underTest.loadBeerToPump(beerRFID, pumpName);

        verify(mockPumpRepository).findByPumpName(pumpName);
        verify(mockPumpRepository).save(pumpCaptor.capture());
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

        Pump savedPump = pumpCaptor.getValue();

        assertEquals(pump.getId(), savedPump.getId());
        assertEquals(pump.getPumpName(), savedPump.getPumpName());
        assertEquals(beerRFID,savedPump.getAssignedRfid());
    }

    @Test
    public void loadBeerToPump_pumpNotExisting() throws Exception {

        String beerRFID = "12345";

        String pumpName = pumps[0];

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(null);

        underTest.loadBeerToPump(beerRFID, pumpName);

        verify(mockPumpRepository).findByPumpName(pumpName);
        verify(mockPumpRepository).save(pumpCaptor.capture());
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

        Pump savedPump = pumpCaptor.getValue();

        // We don't test the id as that is set by the repository (which we have mocked)

        assertEquals(pumpName, savedPump.getPumpName());
        assertEquals(beerRFID,savedPump.getAssignedRfid());
    }

    @Test
    public void registerRating_success() throws Exception {

        String pumpName = pumps[0];
        String beerRFID = "12345";

        Pump pump = new Pump(pumpName);
        pump.setAssignedRfid(beerRFID);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(pump);

        underTest.registerRating(pumpName, 5);

        verify(mockPumpRepository).findByPumpName(pumpName);
        verify(mockRatingRepository).save(ratingCaptor.capture());
        verifyNoMoreInteractions(mockPumpRepository,mockRatingRepository);

        Rating rating = ratingCaptor.getValue();
        assertEquals(beerRFID,rating.getRfid());
        assertEquals(5, rating.getRating());

    }

    @Test
    public void registerRating_noPump() {

        String pumpName = pumps[0];
        String beerRFID = "12345";

        Pump pump = new Pump(pumpName);
        pump.setAssignedRfid(beerRFID);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(null);

        try {
            underTest.registerRating(pumpName, 5);
            fail("BeerwebNotFoundException expected");
        } catch (BeerwebException e) {
            verify(mockPumpRepository).findByPumpName(pumpName);
            verifyNoMoreInteractions(mockPumpRepository,mockRatingRepository);
        }

    }

    @Test
    public void registerRating_noBeerRegister() {

        String pumpName = pumps[0];
        String beerRFID = "12345";

        Pump pump = new Pump(pumpName);

        when(mockPumpRepository.findByPumpName(pumpName)).thenReturn(pump);

        try {
            underTest.registerRating(pumpName, 3);
            fail("BeerwebNotFoundException expected");
        } catch (BeerwebException e) {
            verify(mockPumpRepository).findByPumpName(pumpName);
            verifyNoMoreInteractions(mockPumpRepository,mockRatingRepository);
        }



    }

    @Test
    public void getRatings() throws Exception {

        List<Rating> ratingList = Arrays.asList(ratings);

        when(mockRatingRepository.findAll()).thenReturn(ratingList);

        List<Rating> actual = underTest.getRatings();

        verify(mockRatingRepository).findAll();
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

        assertEquals(ratingList, actual);
    }

    @Test
    public void getRatingsByBeer() throws Exception {

        String beerRFID = "12345";

        List<Rating> ratingList = Arrays.asList(ratings);

        when(mockRatingRepository.findAllByRfidOrderByTimestampDesc(beerRFID)).thenReturn(ratingList.stream().filter(rating -> beerRFID == rating.getRfid()).collect(Collectors.toList()));

        List<Rating> actual = underTest.getRatingsByBeer("12345");

        verify(mockRatingRepository).findAllByRfidOrderByTimestampDesc(beerRFID);
        verifyNoMoreInteractions(mockPumpRepository, mockRatingRepository);

        assertEquals(ratingList.stream().filter(rating -> beerRFID == rating.getRfid()).collect(Collectors.toList()), actual);
    }

}