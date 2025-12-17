package com.max.rental.services;

import com.max.rental.config.RedisCacheConfig;
import com.max.rental.dto.listing.ListingFilterDto;
import com.max.rental.dto.listing.ListingSummaryDto;
import com.max.rental.dto.listing.ListingDetailsDto;
import com.max.rental.dto.listing.PopularCityDto;
import com.max.rental.dto.review.ReviewDto;
import com.max.rental.models.entities.Listing;
import com.max.rental.models.entities.Review;
import com.max.rental.repositories.BookingRepository;
import com.max.rental.repositories.FavoriteRepository;
import com.max.rental.repositories.ListingRepository;
import com.max.rental.repositories.ReviewRepository;
import com.max.rental.security.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListingService {

    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    private final ListingRepository listingRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    public ListingService(ListingRepository listingRepository,
                          ReviewRepository reviewRepository,
                          FavoriteRepository favoriteRepository,
                          BookingRepository bookingRepository,
                          CurrentUserService currentUserService,
                          ModelMapper modelMapper) {
        this.listingRepository = listingRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
        this.modelMapper = modelMapper;
    }

    private String formatSearchParam(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        return "%" + input.trim().toLowerCase() + "%";
    }

    @Transactional(readOnly = true)
    public ListingDetailsDto getListingDetails(Long listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Объявление не найдено: " + listingId));

        return mapToDetailsDto(listing);
    }

    private ListingDetailsDto mapToDetailsDto(Listing listing) {
        ListingDetailsDto dto = modelMapper.map(listing, ListingDetailsDto.class);

        dto.setCity(listing.getAddress().getCity());
        dto.setDistrict(listing.getAddress().getDistrict());
        dto.setPropertyType(listing.getPropertyType().getType());

        dto.setOwnerId(listing.getOwner().getId());
        dto.setOwnerFullName(listing.getOwner().getFullName());
        dto.setOwnerRating(listing.getOwner().getOwnerRating());

        List<Review> reviews = reviewRepository.findByListingIdOrderByCreatedAtDesc(listing.getId());
        dto.setReviews(reviews.stream().map(this::mapToReviewDto).collect(Collectors.toList()));

        dto.setAvailableForBooking(true);

        try {
            Long currentUserId = currentUserService.getCurrentUserId();

            dto.setCurrentUserOwner(listing.getOwner().getId().equals(currentUserId));

            dto.setFavorite(favoriteRepository.existsByRenterIdAndListingId(currentUserId, listing.getId()));

            log.debug("Пользователь {} просматривает объявление {}. Владелец: {}, В избранном: {}",
                    currentUserId, listing.getId(), dto.isCurrentUserOwner(), dto.isFavorite());
        } catch (Exception e) {
            dto.setCurrentUserOwner(false);
            dto.setFavorite(false);
            log.debug("Неавторизованный пользователь просматривает объявление {}", listing.getId());
        }

        return dto;
    }

    private ReviewDto mapToReviewDto(Review review) {
        ReviewDto dto = modelMapper.map(review, ReviewDto.class);
        dto.setGuestName(review.getGuest().getFirstName());

        if (review.getCreatedAt() != null) {
            dto.setReviewDate(review.getCreatedAt().toLocalDate());
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.LISTING_SEARCH_CACHE,
               key = "T(java.util.Objects).hash(#filter.city, #filter.district, #filter.propertyType, " +
                     "#filter.minPrice, #filter.maxPrice, #filter.floor, #filter.startDate, #filter.endDate)")
    public List<ListingSummaryDto> searchListings(ListingFilterDto filter) {
        log.debug("Cache miss - executing search query for filter: {}", filter);

        String cityFilter = formatSearchParam(filter.getCity());
        String districtFilter = formatSearchParam(filter.getDistrict());

        String typeFilter = filter.getPropertyType();
        if (typeFilter != null && !typeFilter.trim().isEmpty()) {
            typeFilter = typeFilter.toUpperCase();
        } else {
            typeFilter = null;
        }

        List<Listing> listings;

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            listings = listingRepository.findWithAllFilters(
                    cityFilter,
                    districtFilter,
                    typeFilter,
                    filter.getMinPrice(),
                    filter.getMaxPrice(),
                    filter.getFloor(),
                    filter.getStartDate(),
                    filter.getEndDate()
            );
        } else {
            listings = listingRepository.findWithBaseFilters(
                    cityFilter,
                    districtFilter,
                    typeFilter,
                    filter.getMinPrice(),
                    filter.getMaxPrice(),
                    filter.getFloor()
            );
        }

        return listings.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ListingSummaryDto mapToDto(Listing listing) {
        ListingSummaryDto dto = modelMapper.map(listing, ListingSummaryDto.class);

        String desc = listing.getDescription();
        dto.setDescription(desc != null && desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);

        if (listing.getAddress() != null) {
            dto.setCity(listing.getAddress().getCity());
            dto.setDistrict(listing.getAddress().getDistrict());
        }
        if (listing.getPropertyType() != null) {
            dto.setType(listing.getPropertyType().getType());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = RedisCacheConfig.POPULAR_CITIES_CACHE, key = "#limit")
    public List<PopularCityDto> getPopularCities(int limit) {
        log.debug("Cache miss - fetching popular cities with limit: {}", limit);
        List<Object[]> cityStats = listingRepository.getCityStatistics();
        Map<String, Long> listingsMap = new HashMap<>();
        for (Object[] row : cityStats) {
            String city = (String) row[0];
            Long count = (Long) row[1];
            listingsMap.put(city, count);
        }

        List<Object[]> bookingStats = bookingRepository.getBookingStatisticsByCity();
        Map<String, Long> bookingsMap = new HashMap<>();
        for (Object[] row : bookingStats) {
            String city = (String) row[0];
            Long count = (Long) row[1];
            bookingsMap.put(city, count);
        }

        Set<String> allCities = new HashSet<>();
        allCities.addAll(listingsMap.keySet());
        allCities.addAll(bookingsMap.keySet());

        List<PopularCityDto> popularCities = allCities.stream()
                .map(city -> new PopularCityDto(
                        city,
                        listingsMap.getOrDefault(city, 0L),
                        bookingsMap.getOrDefault(city, 0L)
                ))
                .sorted(Comparator.comparing(PopularCityDto::getPopularityScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return popularCities;
    }
}
