package com.max.rental.controllers;

import com.max.rental.dto.booking.BookingRequestDto;
import com.max.rental.dto.booking.PriceCalculationDto;
import com.max.rental.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/history")
    public String getBookingHistory(Model model) {
        model.addAttribute("bookings", bookingService.getBookingHistory());
        return "booking-history";
    }

    @PostMapping
    public String createBooking(@ModelAttribute BookingRequestDto bookingRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.createBooking(bookingRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Бронирование успешно создано!");
            return "redirect:/bookings/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/listings/" + bookingRequest.getListingId();
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Бронирование успешно отменено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings/history";
    }

    @GetMapping("/calculate-price")
    @ResponseBody
    public ResponseEntity<PriceCalculationDto> calculatePrice(
            @RequestParam Long listingId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            PriceCalculationDto priceCalculation = bookingService.calculatePrice(listingId, startDate, endDate);
            return ResponseEntity.ok(priceCalculation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}