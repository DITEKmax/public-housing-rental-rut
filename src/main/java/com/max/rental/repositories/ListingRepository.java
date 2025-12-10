// === Файл: src/main/java/com/max/rental/repositories/ListingRepository.java ===
// ОБНОВЛЕННЫЙ ФАЙЛ - добавлены методы для владельца
package com.max.rental.repositories;

import com.max.rental.models.entities.Listing;
import com.max.rental.models.enums.EnumListingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    String BASE_FILTER_CONDITION = "l.status = 'ACTIVE' " +
            "AND (:city IS NULL OR lower(a.city) LIKE :city) " +
            "AND (:district IS NULL OR lower(a.district) LIKE :district) " +
            "AND (:type IS NULL OR pt.type = :type) " +
            "AND (:minPrice IS NULL OR l.pricePerNight >= :minPrice) " +
            "AND (:maxPrice IS NULL OR l.pricePerNight <= :maxPrice) " +
            "AND (:floor IS NULL OR l.floor = :floor) ";

    @Query("SELECT l FROM Listing l " +
            "JOIN l.address a " +
            "JOIN l.propertyType pt " +
            "WHERE " + BASE_FILTER_CONDITION)
    List<Listing> findWithBaseFilters(
            @Param("city") String city,
            @Param("district") String district,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("floor") Integer floor
    );

    @Query("SELECT l FROM Listing l " +
            "JOIN l.address a " +
            "JOIN l.propertyType pt " +
            "WHERE " + BASE_FILTER_CONDITION +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM Booking b " +
            "  WHERE b.listing.id = l.id " +
            "  AND b.status = 'CONFIRMED' " +
            "  AND (b.startDate < :endDate AND b.endDate > :startDate)" +
            ")")
    List<Listing> findWithAllFilters(
            @Param("city") String city,
            @Param("district") String district,
            @Param("type") String type,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("floor") Integer floor,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    List<Listing> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Listing> findAllByOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, EnumListingStatus status);

    Optional<Listing> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, EnumListingStatus status);

    @Query("SELECT a.city as city, COUNT(l) as count " +
            "FROM Listing l JOIN l.address a " +
            "WHERE l.status = 'ACTIVE' " +
            "GROUP BY a.city " +
            "ORDER BY COUNT(l) DESC")
    List<Object[]> getCityStatistics();

    List<Listing> findByCreatedAtAfterOrderByCreatedAtDesc(java.time.LocalDateTime date);
}