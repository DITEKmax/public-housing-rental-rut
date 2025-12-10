package com.max.rental.controllers;

import com.max.rental.models.entities.Booking;
import com.max.rental.models.entities.Listing;
import com.max.rental.models.entities.Review;
import com.max.rental.models.entities.User;
import com.max.rental.repositories.BookingRepository;
import com.max.rental.repositories.ListingRepository;
import com.max.rental.repositories.UserRepository;
import com.max.rental.services.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final BookingRepository bookingRepository;
    private final ReviewService reviewService;

    @GetMapping
    @Transactional(readOnly = true)
    public String adminDashboard(Model model) {

        java.time.LocalDateTime oneDayAgo = java.time.LocalDateTime.now().minusDays(1);

        List<User> recentUsers = userRepository.findByCreatedAtAfterOrderByCreatedAtDesc(oneDayAgo);
        recentUsers.forEach(user -> {
            if (user.getRoles() != null) {
                user.getRoles().size();
            }
        });

        List<Listing> recentListings = listingRepository.findByCreatedAtAfterOrderByCreatedAtDesc(oneDayAgo);
        recentListings.forEach(listing -> {
            if (listing.getAddress() != null) {
                listing.getAddress().getCity();
            }
            if (listing.getOwner() != null) {
                listing.getOwner().getEmail();
            }
        });

        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalListings", listingRepository.count());
        model.addAttribute("totalBookings", bookingRepository.count());
        model.addAttribute("recentUsers", recentUsers);
        model.addAttribute("recentListings", recentListings);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        users.forEach(user -> {
            if (user.getRoles() != null) {
                user.getRoles().size();
            }
        });
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/listings")
    @Transactional(readOnly = true)
    public String listListings(Model model) {
        List<Listing> listings = listingRepository.findAll();
        listings.forEach(listing -> {
            if (listing.getAddress() != null) {
                listing.getAddress().getCity();
            }
            if (listing.getOwner() != null) {
                listing.getOwner().getEmail();
            }
        });
        model.addAttribute("listings", listings);
        return "admin/listings";
    }

    @GetMapping("/bookings")
    @Transactional(readOnly = true)
    public String listBookings(Model model) {
        List<Booking> bookings = bookingRepository.findAll();
        bookings.forEach(booking -> {
            if (booking.getListing() != null) {
                booking.getListing().getTitle();
            }
            if (booking.getRenter() != null) {
                booking.getRenter().getEmail();
            }
        });
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @GetMapping("/reviews")
    @Transactional(readOnly = true)
    public String listReviews(Model model) {
        List<Review> reviews = reviewService.getAllReviews();
        reviews.forEach(review -> {
            if (review.getListing() != null) {
                review.getListing().getTitle();
                if (review.getListing().getAddress() != null) {
                    review.getListing().getAddress().getCity();
                }
                if (review.getListing().getOwner() != null) {
                    review.getListing().getOwner().getEmail();
                }
            }
            if (review.getGuest() != null) {
                review.getGuest().getEmail();
            }
            if (review.getBooking() != null) {
                review.getBooking().getId();
            }
        });
        model.addAttribute("reviews", reviews);
        return "admin/reviews";
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв удалён");
        } catch (Exception e) {
            log.error("Ошибка при удалении отзыва {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления: " + e.getMessage());
        }
        return "redirect:/admin/reviews";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Пользователь удалён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка удаления: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}