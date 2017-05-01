package uk.codersparks.hackspace.beerweb.v2.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.codersparks.hackspace.beerweb.v2.model.Rating;


/**
 * TODO: Add Javadoc
 */
public interface RatingRepository extends PagingAndSortingRepository<Rating, Long> {

    Iterable<Rating> findAllByRfidOrderByTimestampDesc(String rfid);
}
