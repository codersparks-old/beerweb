package uk.codersparks.hackspace.beerweb.v2.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * TODO: Add Javadoc
 */
@Entity
@Data
@NoArgsConstructor
@Table(indexes = {
        @Index(name="PUMP_NAME_IDX", columnList = "pumpName"),
        @Index(name="PUMP_RFID_IDX", columnList = "rfid")
})
public class Pump {

    @Id
    @GeneratedValue
    private long id;

    @Column(unique = true, nullable = false, name = "pumpName")
    private String pumpName;

    @Column(unique = true, name="rfid")
    private String assignedRfid = null;

    @Column(name="beername")
    private String assignedName = null;

    public Pump(String pumpName) {
        this.pumpName = pumpName;
    }
}
