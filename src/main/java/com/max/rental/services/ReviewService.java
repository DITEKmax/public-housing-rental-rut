package com.max.rental.services;

import com.max.rental.models.entities.Booking;
import com.max.rental.models.entities.Listing;
import com.max.rental.models.entities.Review;
import com.max.rental.models.entities.User;
import com.max.rental.models.enums.EnumBookingStatus;
import com.max.rental.repositories.BookingRepository;
import com.max.rental.repositories.ReviewRepository;
import com.max.rental.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Review> getOwnerReviews(Long ownerId) {
        return reviewRepository.findByListingOwnerIdOrderByCreatedAtDesc(ownerId);
    }

    @Transactional(readOnly = true)
    public List<Review> getTenantReviews(Long tenantId) {
        return reviewRepository.findByGuestIdOrderByCreatedAtDesc(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Review> getListingReviews(Long listingId) {
        return reviewRepository.findByListingIdOrderByCreatedAtDesc(listingId);
    }

    @Transactional
    public Review createReview(Long bookingId, Integer rating, String comment) {
        User currentUser = currentUserService.getCurrentUser();
        Long currentUserId = currentUser.getId();

        log.info("Пользователь {} создает отзыв для бронирования {}", currentUserId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        if (!booking.getRenter().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете оставить отзыв для чужого бронирования");
        }

        if (booking.getEndDate().isAfter(LocalDate.now()) || booking.getEndDate().isEqual(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Можно оставить отзыв только после завершения бронирования");
        }

        if (booking.getStatus() == EnumBookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя оставить отзыв для отмененного бронирования");
        }

        if (reviewRepository.findByBookingId(bookingId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Вы уже оставили отзыв для этого бронирования");
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Рейтинг должен быть от 1 до 5");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setListing(booking.getListing());
        review.setGuest(currentUser);
        review.setRating(rating);
        review.setComment(comment);

        Review savedReview = reviewRepository.save(review);
        log.info("Отзыв {} создан для бронирования {}", savedReview.getId(), bookingId);

        updateListingAverageRating(booking.getListing());

        return savedReview;
    }

    private void updateListingAverageRating(Listing listing) {
        List<Review> reviews = reviewRepository.findByListingIdOrderByCreatedAtDesc(listing.getId());
        if (!reviews.isEmpty()) {
            double averageRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            listing.setAverageRating(averageRating);
        }
    }


    @Transactional(readOnly = true)
    public List<Booking> getCompletedBookingsWithoutReviews() {
        Long currentUserId = currentUserService.getCurrentUserId();
        List<Booking> allBookings = bookingRepository.findAllByRenterIdWithListing(currentUserId);

        LocalDate today = LocalDate.now();

        return allBookings.stream()
                .filter(booking -> booking.getStatus() == EnumBookingStatus.CONFIRMED)
                .filter(booking -> booking.getEndDate().isBefore(today))
                .filter(booking -> reviewRepository.findByBookingId(booking.getId()).isEmpty())
                .toList();
    }


    @Transactional
    public void deleteReview(Long reviewId) {
        User currentUser = currentUserService.getCurrentUser();

        log.info("Пользователь {} пытается удалить отзыв {}", currentUser.getId(), reviewId);

        if (!currentUser.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Только администратор может удалять отзывы");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Отзыв не найден"));

        Listing listing = review.getListing();

        reviewRepository.delete(review);
        log.info("Отзыв {} удален администратором {}", reviewId, currentUser.getId());

        updateListingAverageRating(listing);
    }
}
