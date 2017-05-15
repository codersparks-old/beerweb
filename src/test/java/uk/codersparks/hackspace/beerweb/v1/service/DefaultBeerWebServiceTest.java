package uk.codersparks.hackspace.beerweb.v1.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import uk.codersparks.hackspace.beerweb.v1.exception.BeerWebException;
import uk.codersparks.hackspace.beerweb.v1.model.Beer;
import uk.codersparks.hackspace.beerweb.v1.repository.BeerRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * TODO: Add Javadoc
 */
public class DefaultBeerWebServiceTest {

    @InjectMocks
    private DefaultBeerWebService underTest;

    @Mock
    private BeerRepository beerRepository;

    @Mock
    private Beer mockBeer;

    private ArgumentCaptor<Beer> beerArgumentCaptor;

    private String rfId;

    private String pumpId;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);

        beerArgumentCaptor = ArgumentCaptor.forClass(Beer.class);

        rfId = "rfId1";
        pumpId = "pump01";

        underTest = new DefaultBeerWebService(beerRepository);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testRegisterBeerRating_zeroSupplied_NoExistingBeerNoLoadedBeer() throws Exception {

        when(beerRepository.exists(rfId)).thenReturn(false);

        when(beerRepository.findByPumpId(pumpId)).thenReturn(null);

        underTest.registerBeerRating(pumpId, rfId, 0);

        verify(beerRepository, times(1)).exists(rfId);
        verify(beerRepository, times(1)).findByPumpId(pumpId);
        verify(beerRepository, times(1)).save(beerArgumentCaptor.capture());
        verifyNoMoreInteractions(beerRepository);

        Beer capturedBeer = beerArgumentCaptor.getValue();

        assertEquals(rfId, capturedBeer.getRfid());
        assertEquals(pumpId, capturedBeer.getPumpId());
        assertEquals(0, capturedBeer.getNumRatings());
        assertEquals(0, capturedBeer.getRunningTotalRating());

        assertNotNull(capturedBeer.getLastVote());
    }

    @Test
    public void testRegisterBeerRating_zeroSupplied_WithExistingRFID() throws Exception {

        when(beerRepository.exists(rfId)).thenReturn(true);

        underTest.registerBeerRating(pumpId, rfId, 0);

        verify(beerRepository, times(1)).exists(rfId);

        verifyNoMoreInteractions(beerRepository);

    }

    @Test
    public void testRegisterBeerRating_zeroSupplied_NoExistingBeerWithAlreadyLoadedBeer() throws Exception {

        when(beerRepository.exists(rfId)).thenReturn(false);

        when(beerRepository.findByPumpId(pumpId)).thenReturn(mockBeer);

        underTest.registerBeerRating(pumpId, rfId, 0);

        InOrder inOrder = inOrder(beerRepository, mockBeer);

        inOrder.verify(beerRepository, times(1)).findByPumpId(pumpId);
        inOrder.verify(mockBeer, times(1)).setPumpId(null);
        inOrder.verify(beerRepository, times(1)).save(mockBeer);

        inOrder.verify(beerRepository, times(1)).save(beerArgumentCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        Beer capturedBeer = beerArgumentCaptor.getValue();

        assertNotEquals(capturedBeer, mockBeer);

        assertEquals(rfId, capturedBeer.getRfid());
        assertEquals(pumpId, capturedBeer.getPumpId());
        assertEquals(0, capturedBeer.getNumRatings());
        assertEquals(0, capturedBeer.getRunningTotalRating());

        assertNotNull(capturedBeer.getLastVote());
    }

    @Test
    public void testRegisterBeerRating_withRating_beerExists() throws Exception {


        int initialRunningTotal = 4;
        int initialNumRatings = 22;
        when(mockBeer.getRunningTotalRating()).thenReturn(initialRunningTotal);
        when(mockBeer.getNumRatings()).thenReturn(initialNumRatings);

        when(beerRepository.findOne(rfId)).thenReturn(mockBeer);

        underTest.registerBeerRating(pumpId, rfId, 3);

        InOrder inOrder = inOrder(beerRepository, mockBeer);

        inOrder.verify(beerRepository).findOne(rfId);
        inOrder.verify(mockBeer).getRunningTotalRating();
        inOrder.verify(mockBeer).setRunningTotalRating(initialRunningTotal + 3);
        inOrder.verify(mockBeer).getNumRatings();
        inOrder.verify(mockBeer).setNumRatings(initialNumRatings + 1);
        inOrder.verify(mockBeer).setLastVote(any(String.class));
        inOrder.verify(beerRepository).save(mockBeer);
        inOrder.verifyNoMoreInteractions();

    }

    @Test(expected = BeerWebException.class)
    public void testRegisterBeerRating_withRating_beerNotExisting() throws Exception {

        try {
            underTest.registerBeerRating(pumpId, rfId, 3);
        } catch(Exception e) {
            verify(beerRepository, times(1)).findOne(rfId);
            verifyNoMoreInteractions(beerRepository, mockBeer);
            throw e;
        }
    }

    @Test(expected = BeerWebException.class)
    public void testRegisterBeerRating_outOfRangeRatingSupplied() throws Exception {

        try {
            underTest.registerBeerRating(pumpId, rfId, 6);
        } catch (Exception e) {
            verifyNoMoreInteractions(beerRepository, mockBeer);
            throw e;
        }
    }

    @Test
    public void testGetAllPumpData_NoPumpsInitialised() throws Exception {

        when(beerRepository.findByPumpIdIsNotNull()).thenReturn(null);

        Map<String, Beer> pumpMap = underTest.getAllPumpData();

        verify(beerRepository).findByPumpIdIsNotNull();
        verifyNoMoreInteractions(beerRepository);

        assertEquals(Collections.emptyMap(), pumpMap);
    }

    @Test
    public void testGetAllPumpData_PumpsInitiailised() throws Exception {
        String pump1 = "pump1";
        String pump2 = "pump2";

        String rfid1 = "rfid1";
        String rfid2 = "rfid2";

        int numRatings1 = 3;
        int numRatings2 = 5;

        int runTot1 = 5;
        int runTot2 = 11;

        Beer beer1 = generateBeer(rfid1, pump1, numRatings1, runTot1);
        Beer beer2 = generateBeer(rfid2, pump2, numRatings2, runTot2);

        when(beerRepository.findByPumpIdIsNotNull()).thenReturn(Arrays.asList(beer1, beer2));

        Map<String, Beer> pumpMap = underTest.getAllPumpData();

        verify(beerRepository).findByPumpIdIsNotNull();
        verifyNoMoreInteractions(beerRepository);

        // Test size of map and keys
        assertEquals(2, pumpMap.keySet().size());
        assertTrue(pumpMap.containsKey(pump1));
        assertTrue(pumpMap.containsKey(pump2));

        Beer actualBeer1 = pumpMap.get(pump1);
        assertEquals(beer1, actualBeer1);

        Beer actualBeer2 = pumpMap.get(pump2);
        assertEquals(beer2, actualBeer2);

    }


    private Beer generateBeer(String rfid, String pumpId, int numRatings, int runningTotal) {
        Beer beer = new Beer();
        beer.setRfid(rfid);
        beer.setPumpId(pumpId);
        beer.setRunningTotalRating(runningTotal);
        beer.setNumRatings(numRatings);
        beer.setLastVote("Some Date String");
        return beer;
    }


}