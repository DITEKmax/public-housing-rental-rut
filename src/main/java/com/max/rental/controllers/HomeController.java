package com.max.rental.controllers;

import com.max.rental.dto.listing.ListingFilterDto;
import com.max.rental.dto.listing.ListingSummaryDto;
import com.max.rental.dto.listing.PopularCityDto;
import com.max.rental.services.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ListingService listingService;

    @GetMapping("/")
    public String home(Model model) {
        List<ListingSummaryDto> featuredListings = listingService
                .searchListings(new ListingFilterDto())
                .stream()
                .limit(6)
                .collect(Collectors.toList());

        List<PopularCityDto> popularCities = listingService.getPopularCities(6);

        model.addAttribute("listings", featuredListings);
        model.addAttribute("popularCities", popularCities);
        return "home";
    }
}