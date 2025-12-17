package com.max.rental.models.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id")
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private User guest;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public User getGuest() {
        return guest;
    }

    public void setGuest(User guest) {
        this.guest = guest;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(getId(), review.getId()) &&
               Objects.equals(getCreatedAt(), review.getCreatedAt()) &&
               Objects.equals(rating, review.rating) &&
               Objects.equals(comment, review.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), rating, comment);
    }

    @Override
    public String toString() {
        return "Review{" +
               "id=" + getId() +
               ", createdAt=" + getCreatedAt() +
               ", rating=" + rating +
               ", comment='" + comment + '\'' +
               '}';
    }
}
