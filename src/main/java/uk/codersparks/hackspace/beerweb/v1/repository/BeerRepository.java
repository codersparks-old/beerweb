package uk.codersparks.hackspace.beerweb.v1.repository;

import org.springframework.data.repository.CrudRepository;
import uk.codersparks.hackspace.beerweb.v1.model.Beer;

import java.util.Collection;

/**
 * TODO: Add Javadoc
 */
public interface BeerRepository extends CrudRepository<Beer, String> {

    Beer findByPumpId(String pumpId);

    Collection<Beer> findByPumpIdIsNotNull();

}
