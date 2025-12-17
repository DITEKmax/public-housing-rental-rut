package com.max.rental.models.entities;

import com.max.rental.models.enums.EnumBookingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnumBookingStatus status;

    public Listing getListing() {
        return listing;
    }

    public void setListing(Listing listing) {
        this.listing = listing;
    }

    public User getRenter() {
        return renter;
    }

    public void setRenter(User renter) {
        this.renter = renter;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public EnumBookingStatus getStatus() {
        return status;
    }

    public void setStatus(EnumBookingStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(getId(), booking.getId()) &&
               Objects.equals(getCreatedAt(), booking.getCreatedAt()) &&
               Objects.equals(startDate, booking.startDate) &&
               Objects.equals(endDate, booking.endDate) &&
               Objects.equals(totalPrice, booking.totalPrice) &&
               status == booking.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCreatedAt(), startDate, endDate, totalPrice, status);
    }

    @Override
    public String toString() {
        return "Booking{" +
               "id=" + getId() +
               ", createdAt=" + getCreatedAt() +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", totalPrice=" + totalPrice +
               ", status=" + status +
               '}';
    }
}
