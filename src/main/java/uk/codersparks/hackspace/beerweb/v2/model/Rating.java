package uk.codersparks.hackspace.beerweb.v2.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * TODO: Add Javadoc
 */
@Entity
@Data
@Table(indexes = {
        @Index(name="RATING_RFID_IDX", columnList = "rfid"),
        @Index(name="RATING_TIME_IDX", columnList = "timestamp")
})
public class Rating {

    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false)
    private String rfid;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
