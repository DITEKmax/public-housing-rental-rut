package com.max.rental.controllers;

import com.max.rental.dto.listing.ListingDetailsDto;
import com.max.rental.dto.listing.ListingFilterDto;
import com.max.rental.dto.listing.ListingSummaryDto;
import com.max.rental.services.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping("/{id}")
    public String getListingDetails(@PathVariable Long id, Model model) {
        ListingDetailsDto details = listingService.getListingDetails(id);
        model.addAttribute("listing", details);
        return "listing-details";
    }

    @GetMapping
    public String getListings(ListingFilterDto filterDto,
                              @RequestParam(required = false) String sortBy,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "9") int size,
                              Model model) {
        List<ListingSummaryDto> listings = listingService.searchListings(filterDto);

        if (sortBy != null && !sortBy.isEmpty()) {
            listings = applySorting(listings, sortBy);
        }

        int totalListings = listings.size();
        int totalPages = (int) Math.ceil((double) totalListings / size);

        if (page < 0) page = 0;
        if (page >= totalPages && totalPages > 0) page = totalPages - 1;

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalListings);

        List<ListingSummaryDto> paginatedListings = totalListings > 0
            ? listings.subList(fromIndex, toIndex)
            : listings;

        model.addAttribute("listings", paginatedListings);
        model.addAttribute("filter", filterDto);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalListings", totalListings);
        return "listings";
    }

    private List<ListingSummaryDto> applySorting(List<ListingSummaryDto> listings, String sortBy) {
        switch (sortBy) {
            case "price_asc":
                return listings.stream()
                        .sorted(Comparator.comparing(ListingSummaryDto::getPricePerNight))
                        .collect(Collectors.toList());
            case "price_desc":
                return listings.stream()
                        .sorted(Comparator.comparing(ListingSummaryDto::getPricePerNight).reversed())
                        .collect(Collectors.toList());
            case "rating_desc":
                return listings.stream()
                        .sorted(Comparator.comparing(ListingSummaryDto::getRating,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.toList());
            default:
                return listings;
        }
    }
}