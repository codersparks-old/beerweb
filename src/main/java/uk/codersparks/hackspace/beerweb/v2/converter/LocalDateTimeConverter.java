package uk.codersparks.hackspace.beerweb.v2.converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.*;
import java.util.Date;

/**
 * TODO: Add Javadoc
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

    private static final Logger logger = LoggerFactory.getLogger(LocalDateTimeConverter.class);

    @Override
    public Date convertToDatabaseColumn(LocalDateTime localDateTime) {

        Instant instant = localDateTime.atZone(ZoneId.of("Europe/London")).toInstant();
        Date date = Date.from(instant);
        logger.debug("Converted localDateTime: {} to Date: {} for persistance", localDateTime, date);
        return date;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Date date) {

        ZonedDateTime instant = date.toInstant().atZone(ZoneId.of("Europe/London"));
        LocalDateTime localDateTime = LocalDateTime.from(instant);
        logger.debug("Converted Date: {} to LocalDateTime: {} from persistance", date, localDateTime);
        return localDateTime;
    }
}
