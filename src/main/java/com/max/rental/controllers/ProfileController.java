package com.max.rental.controllers;

import com.max.rental.dto.booking.BookingHistoryDto;
import com.max.rental.dto.profile.ProfileDto;
import com.max.rental.dto.profile.ProfileUpdateDto;
import com.max.rental.services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public String getProfile(Model model) {
        ProfileDto profile = profileService.getProfile();
        model.addAttribute("profile", profile);
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        ProfileDto profile = profileService.getProfile();
        model.addAttribute("profile", profile);
        model.addAttribute("updateDto", new ProfileUpdateDto());
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(@Valid @ModelAttribute("updateDto") ProfileUpdateDto updateDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ProfileDto profile = profileService.getProfile();
            model.addAttribute("profile", profile);
            return "profile-edit";
        }

        try {
            profileService.updateProfile(updateDto);
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile/edit";
        }
        return "redirect:/profile";
    }

    @GetMapping("/bookings")
    public String getBookingHistory(Model model) {
        List<BookingHistoryDto> bookings = profileService.getBookingHistory();
        model.addAttribute("bookings", bookings);
        return "booking-history";
    }
}