package com.max.rental.controllers;

import com.max.rental.dto.booking.OwnerBookingDto;
import com.max.rental.dto.listing.ListingCreateDto;
import com.max.rental.dto.listing.ListingEditDto;
import com.max.rental.dto.listing.OwnerListingDto;
import com.max.rental.models.enums.EnumListingStatus;
import com.max.rental.repositories.PropertyTypeRepository;
import com.max.rental.services.OwnerListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/owner/listings")
@RequiredArgsConstructor
public class OwnerListingController {

    private final OwnerListingService ownerListingService;
    private final PropertyTypeRepository propertyTypeRepository;

    @GetMapping
    public String getMyListings(@RequestParam(required = false) String status, Model model) {
        List<OwnerListingDto> listings;

        if (status != null && !status.isEmpty()) {
            try {
                EnumListingStatus enumStatus = EnumListingStatus.valueOf(status.toUpperCase());
                listings = ownerListingService.getMyListingsByStatus(enumStatus);
            } catch (IllegalArgumentException e) {
                listings = ownerListingService.getMyListings();
            }
        } else {
            listings = ownerListingService.getMyListings();
        }

        model.addAttribute("listings", listings);
        model.addAttribute("stats", ownerListingService.getOwnerStats());
        model.addAttribute("currentStatus", status);
        return "owner/my-listings";
    }

    @GetMapping("/new")
    public String newListingForm(Model model) {
        model.addAttribute("listing", new ListingCreateDto());
        model.addAttribute("propertyTypes", propertyTypeRepository.findAll());
        model.addAttribute("statuses", EnumListingStatus.values());
        return "owner/listing-form";
    }

    @PostMapping
    public String createListing(@ModelAttribute ListingCreateDto dto, RedirectAttributes redirectAttributes) {
        try {
            Long listingId = ownerListingService.createListing(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Объявление успешно создано! ID: " + listingId);
            return "redirect:/owner/listings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owner/listings/new";
        }
    }

    @GetMapping("/{id}")
    public String viewListing(@PathVariable Long id, Model model) {
        try {
            OwnerListingDto listing = ownerListingService.getMyListing(id);
            model.addAttribute("listing", listing);
            return "owner/listing-details";
        } catch (Exception e) {
            return "redirect:/owner/listings";
        }
    }

    @GetMapping("/{id}/edit")
    public String editListingForm(@PathVariable Long id, Model model) {
        try {
            ListingEditDto listing = ownerListingService.getListingForEdit(id);
            model.addAttribute("listing", listing);
            model.addAttribute("propertyTypes", propertyTypeRepository.findAll());
            model.addAttribute("statuses", EnumListingStatus.values());
            return "owner/listing-edit";
        } catch (Exception e) {
            return "redirect:/owner/listings";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateListing(@PathVariable Long id,
                                @ModelAttribute ListingEditDto dto,
                                RedirectAttributes redirectAttributes) {
        try {
            ownerListingService.updateListing(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление успешно обновлено!");
            return "redirect:/owner/listings/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/owner/listings/" + id + "/edit";
        }
    }

    @GetMapping("/{id}/bookings")
    public String viewListingBookings(@PathVariable Long id, Model model) {
        try {
            List<OwnerBookingDto> bookings = ownerListingService.getListingBookings(id);
            String listingTitle = ownerListingService.getListingTitle(id);

            model.addAttribute("bookings", bookings);
            model.addAttribute("listingId", id);
            model.addAttribute("listingTitle", listingTitle);
            return "owner/listing-bookings";
        } catch (Exception e) {
            return "redirect:/owner/listings";
        }
    }

    @PostMapping("/{id}/activate")
    public String activateListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ownerListingService.changeListingStatus(id, EnumListingStatus.ACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление активировано!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/listings";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ownerListingService.changeListingStatus(id, EnumListingStatus.INACTIVE);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление деактивировано.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/listings";
    }

    @PostMapping("/{id}/draft")
    public String draftListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ownerListingService.changeListingStatus(id, EnumListingStatus.DRAFT);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление переведено в черновик.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/listings";
    }

    @PostMapping("/{id}/delete")
    public String deleteListing(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ownerListingService.deleteListing(id);
            redirectAttributes.addFlashAttribute("successMessage", "Объявление удалено.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/listings";
    }
}