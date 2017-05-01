package uk.codersparks.hackspace.beerweb.v2.converter;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * TODO: Add Javadoc
 */
public class LocalDateTimeConverterTest {

    private final static SimpleDateFormat ISODATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


    private LocalDateTimeConverter underTest;

    private final String dateString = "2011-12-03T10:15:30";

    @Before
    public void setup() {

        underTest = new LocalDateTimeConverter();

    }

    @Test
    public void convertToDatabaseColumn() throws Exception {

        LocalDateTime localDateTime = LocalDateTime.parse(dateString);
        Date expected = ISODATEFORMAT.parse(dateString);


        Date actual = underTest.convertToDatabaseColumn(localDateTime);

        assertEquals(expected, actual);

    }

    @Test
    public void convertToEntityAttribute() throws Exception {

        LocalDateTime expected = LocalDateTime.parse(dateString);

        Date date = ISODATEFORMAT.parse(dateString);

        LocalDateTime actual = underTest.convertToEntityAttribute(date);

        assertEquals(expected, actual);

    }

}