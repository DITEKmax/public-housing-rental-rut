package com.max.rental.controllers;

import com.max.rental.dto.favorite.FavoriteListingDto;
import com.max.rental.services.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public String getFavorites(Model model) {
        List<FavoriteListingDto> favorites = favoriteService.getFavorites();
        model.addAttribute("favorites", favorites);
        return "favorites";
    }

    @PostMapping("/add/{listingId}")
    public String addToFavorites(@PathVariable Long listingId,
                                 @RequestHeader(value = "Referer", required = false) String referer,
                                 RedirectAttributes redirectAttributes) {
        try {
            favoriteService.addToFavorites(listingId);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление добавлено в избранное!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/listings/" + listingId;
    }

    @PostMapping("/remove/{listingId}")
    public String removeFromFavorites(@PathVariable Long listingId,
                                      @RequestHeader(value = "Referer", required = false) String referer,
                                      RedirectAttributes redirectAttributes) {
        try {
            favoriteService.removeFromFavorites(listingId);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление удалено из избранного.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/favorites";
    }

    @PostMapping("/toggle/{listingId}")
    public String toggleFavorite(@PathVariable Long listingId,
                                 @RequestHeader(value = "Referer", required = false) String referer,
                                 RedirectAttributes redirectAttributes) {
        try {
            boolean isNowFavorite = favoriteService.toggleFavorite(listingId);
            if (isNowFavorite) {
                redirectAttributes.addFlashAttribute("successMessage", "Добавлено в избранное!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Удалено из избранного.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/listings";
    }
}