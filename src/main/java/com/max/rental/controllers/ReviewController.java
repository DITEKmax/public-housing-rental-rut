package com.max.rental.controllers;

import com.max.rental.models.entities.Booking;
import com.max.rental.models.entities.Review;
import com.max.rental.models.entities.User;
import com.max.rental.security.CurrentUserService;
import com.max.rental.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserService currentUserService;

    @GetMapping
    public String getAllReviews(Model model) {
        User currentUser = currentUserService.getCurrentUser();
        model.addAttribute("isAdmin", currentUser.isAdmin());

        if (currentUser.isAdmin()) {
            List<Review> reviews = reviewService.getAllReviews();
            model.addAttribute("reviews", reviews);
            model.addAttribute("pageTitle", "Все отзывы");
            return "reviews/list";
        } else if (currentUser.isOwner()) {
            List<Review> reviews = reviewService.getOwnerReviews(currentUser.getId());
            model.addAttribute("reviews", reviews);
            model.addAttribute("pageTitle", "Отзывы на мои объявления");
            return "reviews/list";
        } else {
            List<Review> reviews = reviewService.getTenantReviews(currentUser.getId());
            model.addAttribute("reviews", reviews);
            model.addAttribute("pageTitle", "Мои отзывы");
            return "reviews/list";
        }
    }

    @GetMapping("/create")
    public String showCreateReviewPage(@RequestParam(required = false) Long bookingId, Model model) {
        List<Booking> bookingsWithoutReviews = reviewService.getCompletedBookingsWithoutReviews();
        model.addAttribute("bookings", bookingsWithoutReviews);
        model.addAttribute("selectedBookingId", bookingId);
        return "reviews/create";
    }

    @PostMapping("/create")
    public String createReview(@RequestParam Long bookingId,
                               @RequestParam Integer rating,
                               @RequestParam(required = false) String comment,
                               RedirectAttributes redirectAttributes) {
        try {
            reviewService.createReview(bookingId, rating, comment);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв успешно добавлен!");
            return "redirect:/reviews";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/reviews/create";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв успешно удален!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/reviews";
    }
}
