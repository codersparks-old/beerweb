package uk.codersparks.hackspace.beerweb.v2.repository;

import org.springframework.data.repository.CrudRepository;
import uk.codersparks.hackspace.beerweb.v2.model.Pump;

/**
 * TODO: Add Javadoc
 */
public interface PumpRepository extends CrudRepository<Pump, Long> {

    Pump findByPumpName(String name);
}
