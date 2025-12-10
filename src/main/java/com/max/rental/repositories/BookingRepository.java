package com.max.rental.repositories;

import com.max.rental.models.entities.Booking;
import com.max.rental.models.enums.EnumBookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByRenterId(Long renterId);

    List<Booking> findAllByListingIdOrderByStartDateDesc(Long listingId);

    List<Booking> findAllByListingIdAndStatusOrderByStartDateDesc(Long listingId, EnumBookingStatus status);

    long countByListingIdAndStatus(Long listingId, EnumBookingStatus status);

    List<Booking> findAllByListingId(Long listingId);


    @Query("SELECT b FROM Booking b JOIN FETCH b.listing WHERE b.renter.id = :renterId")
    List<Booking> findAllByRenterIdWithListing(Long renterId);


    @Query("SELECT a.city as city, COUNT(b) as count " +
            "FROM Booking b JOIN b.listing l JOIN l.address a " +
            "WHERE b.status = 'CONFIRMED' " +
            "GROUP BY a.city " +
            "ORDER BY COUNT(b) DESC")
    List<Object[]> getBookingStatisticsByCity();
}