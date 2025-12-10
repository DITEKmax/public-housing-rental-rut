package com.max.rental.repositories;

import com.max.rental.models.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.listing l " +
            "LEFT JOIN FETCH l.address " +
            "LEFT JOIN FETCH r.guest " +
            "WHERE r.listing.id = :listingId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByListingIdOrderByCreatedAtDesc(@Param("listingId") Long listingId);

    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.listing l " +
            "LEFT JOIN FETCH l.address " +
            "LEFT JOIN FETCH r.guest " +
            "WHERE r.guest.id = :guestId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByGuestIdOrderByCreatedAtDesc(@Param("guestId") Long guestId);

    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.listing l " +
            "LEFT JOIN FETCH l.address " +
            "LEFT JOIN FETCH l.owner " +
            "LEFT JOIN FETCH r.guest " +
            "WHERE l.owner.id = :ownerId " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByListingOwnerIdOrderByCreatedAtDesc(@Param("ownerId") Long ownerId);

    Optional<Review> findByBookingId(Long bookingId);

    @Query("SELECT r FROM Review r " +
            "LEFT JOIN FETCH r.listing l " +
            "LEFT JOIN FETCH l.address " +
            "LEFT JOIN FETCH r.guest " +
            "ORDER BY r.createdAt DESC")
    List<Review> findAllByOrderByCreatedAtDesc();
}