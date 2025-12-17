package com.max.rental.services;

import com.max.rental.config.RedisCacheConfig;
import com.max.rental.dto.booking.OwnerBookingDto;
import com.max.rental.dto.listing.ListingCreateDto;
import com.max.rental.dto.listing.ListingEditDto;
import com.max.rental.dto.listing.OwnerListingDto;
import com.max.rental.models.entities.Address;
import com.max.rental.models.entities.Booking;
import com.max.rental.models.entities.Listing;
import com.max.rental.models.entities.PropertyType;
import com.max.rental.models.entities.User;
import com.max.rental.models.enums.EnumBookingStatus;
import com.max.rental.models.enums.EnumListingStatus;
import com.max.rental.repositories.BookingRepository;
import com.max.rental.repositories.ListingRepository;
import com.max.rental.repositories.PropertyTypeRepository;
import com.max.rental.security.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerListingService {

    private static final Logger log = LoggerFactory.getLogger(OwnerListingService.class);

    private final ListingRepository listingRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    public OwnerListingService(ListingRepository listingRepository,
                               PropertyTypeRepository propertyTypeRepository,
                               BookingRepository bookingRepository,
                               CurrentUserService currentUserService,
                               ModelMapper modelMapper) {
        this.listingRepository = listingRepository;
        this.propertyTypeRepository = propertyTypeRepository;
        this.bookingRepository = bookingRepository;
        this.currentUserService = currentUserService;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public List<OwnerListingDto> getMyListings() {
        Long ownerId = currentUserService.getCurrentUserId();
        List<Listing> listings = listingRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId);
        return listings.stream().map(this::mapToOwnerDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public List<OwnerListingDto> getMyListingsByStatus(EnumListingStatus status) {
        Long ownerId = currentUserService.getCurrentUserId();
        List<Listing> listings = listingRepository.findAllByOwnerIdAndStatusOrderByCreatedAtDesc(ownerId, status);
        return listings.stream().map(this::mapToOwnerDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public OwnerListingDto getMyListing(Long listingId) {
        Long ownerId = currentUserService.getCurrentUserId();
        Listing listing = listingRepository.findByIdAndOwnerId(listingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Объявление не найдено или вы не являетесь его владельцем"));
        return mapToOwnerDto(listing);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ListingEditDto getListingForEdit(Long listingId) {
        Long ownerId = currentUserService.getCurrentUserId();
        Listing listing = listingRepository.findByIdAndOwnerId(listingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Объявление не найдено"));
        return mapToEditDto(listing);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public List<OwnerBookingDto> getListingBookings(Long listingId) {
        Long ownerId = currentUserService.getCurrentUserId();

        Listing listing = listingRepository.findByIdAndOwnerId(listingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Объявление не найдено или вы не являетесь его владельцем"));

        log.info("Владелец {} запрашивает бронирования для объявления {}", ownerId, listingId);

        List<Booking> bookings = bookingRepository.findAllByListingIdOrderByStartDateDesc(listingId);

        return bookings.stream()
                .map(this::mapToOwnerBookingDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public String getListingTitle(Long listingId) {
        Long ownerId = currentUserService.getCurrentUserId();
        Listing listing = listingRepository.findByIdAndOwnerId(listingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));
        return listing.getTitle();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.LISTING_SEARCH_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.POPULAR_CITIES_CACHE, allEntries = true)
    })
    public Long createListing(ListingCreateDto dto) {
        User owner = currentUserService.getCurrentUser();
        log.info("Пользователь {} создаёт объявление: {}", owner.getEmail(), dto.getTitle());

        validateCreateDto(dto);

        PropertyType propertyType = propertyTypeRepository.findByType(dto.getPropertyType().toUpperCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Неизвестный тип жилья: " + dto.getPropertyType()));

        Address address = new Address();
        address.setCity(dto.getCity().trim());
        address.setDistrict(dto.getDistrict() != null ? dto.getDistrict().trim() : null);

        Listing listing = new Listing();
        listing.setOwner(owner);
        listing.setPropertyType(propertyType);
        listing.setAddress(address);
        listing.setTitle(dto.getTitle().trim());
        listing.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        listing.setPricePerNight(dto.getPricePerNight());
        listing.setRoomCount(dto.getRoomCount());
        listing.setFloor(dto.getFloor());
        listing.setTotalFloors(dto.getTotalFloors());
        listing.setConstructionYear(dto.getConstructionYear());
        listing.setRules(dto.getRules() != null ? dto.getRules().trim() : null);
        listing.setAverageRating(0.0);
        listing.setStatus("ACTIVE".equalsIgnoreCase(dto.getStatus()) ?
                EnumListingStatus.ACTIVE : EnumListingStatus.DRAFT);

        Listing saved = listingRepository.save(listing);
        return saved.getId();
    }

    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.LISTING_SEARCH_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.POPULAR_CITIES_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.LISTING_DETAILS_CACHE, key = "#listingId")
    })
    public void updateListing(Long listingId, ListingEditDto dto) {
        Long ownerId = currentUserService.getCurrentUserId();
        Listing listing = listingRepository.findByIdAndOwnerId(listingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));

        if (dto.getTitle() != null) listing.setTitle(dto.getTitle().trim());
        if (dto.getDescription() != null) listing.setDescription(dto.getDescription().trim());
        if (dto.getPricePerNight() != null) listing.setPricePerNight(dto.getPricePerNight());
        if (dto.getRoomCount() != null) listing.setRoomCount(dto.getRoomCount());
        if (dto.getFloor() != null) listing.setFloor(dto.getFloor());
        if (dto.getTotalFloors() != null) listing.setTotalFloors(dto.getTotalFloors());
        if (dto.getConstructionYear() != null) listing.setConstructionYear(dto.getConstructionYear());
        if (dto.getRules() != null) listing.setRules(dto.getRules().trim());
        if (dto.getCity() != null) listing.getAddress().setCity(dto.getCity().trim());
        if (dto.getDistrict() != null) listing.getAddress().setDistrict(dto.getDistrict().trim());

        if (dto.getPropertyType() != null) {
            PropertyType pt = propertyTypeRepository.findByType(dto.getPropertyType().toUpperCase())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неизвестный тип"));
            listing.setPropertyType(pt);
        }

        if (dto.getStatus() != null) {
            listing.setStatus(EnumListingStatus.valueOf(dto.getStatus().toUpperCase()));
        }

        listingRepository.save(listing);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.LISTING_SEARCH_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.POPULAR_CITIES_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.LISTING_DETAILS_CACHE, key = "#listingId")
    })
    public void changeListingStatus(Long listingId, EnumListingStatus newStatus) {
        Long ownerId = currentUserService.getCurrentUserId();
        Listing listing = listingRepository.findByIdAndOwnerId(listingId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));
        listing.setStatus(newStatus);
        listingRepository.save(listing);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public void deleteListing(Long listingId) {
        changeListingStatus(listingId, EnumListingStatus.INACTIVE);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public OwnerStatsDto getOwnerStats() {
        Long ownerId = currentUserService.getCurrentUserId();
        OwnerStatsDto stats = new OwnerStatsDto();
        stats.setTotalListings(listingRepository.countByOwnerId(ownerId));
        stats.setActiveListings(listingRepository.countByOwnerIdAndStatus(ownerId, EnumListingStatus.ACTIVE));
        stats.setDraftListings(listingRepository.countByOwnerIdAndStatus(ownerId, EnumListingStatus.DRAFT));
        stats.setInactiveListings(listingRepository.countByOwnerIdAndStatus(ownerId, EnumListingStatus.INACTIVE));
        return stats;
    }

    private void validateCreateDto(ListingCreateDto dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название обязательно");
        if (dto.getPricePerNight() == null || dto.getPricePerNight().doubleValue() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Цена должна быть больше 0");
        if (dto.getPropertyType() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Тип жилья обязателен");
        if (dto.getCity() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Город обязателен");
    }

    private OwnerListingDto mapToOwnerDto(Listing listing) {
        OwnerListingDto dto = modelMapper.map(listing, OwnerListingDto.class);

        if (listing.getAddress() != null) {
            dto.setCity(listing.getAddress().getCity());
            dto.setDistrict(listing.getAddress().getDistrict());
        }
        if (listing.getPropertyType() != null) {
            dto.setPropertyType(listing.getPropertyType().getType());
        }

        if (listing.getBookings() != null) {
            dto.setTotalBookings(listing.getBookings().size());
            dto.setActiveBookings((int) listing.getBookings().stream()
                    .filter(b -> b.getStatus() == EnumBookingStatus.CONFIRMED && b.getEndDate().isAfter(LocalDate.now()))
                    .count());
        }
        if (listing.getReviews() != null) {
            dto.setReviewCount(listing.getReviews().size());
        }
        return dto;
    }

    private ListingEditDto mapToEditDto(Listing listing) {
        ListingEditDto dto = modelMapper.map(listing, ListingEditDto.class);

        if (listing.getPropertyType() != null) {
            dto.setPropertyType(listing.getPropertyType().getType());
        }
        if (listing.getAddress() != null) {
            dto.setCity(listing.getAddress().getCity());
            dto.setDistrict(listing.getAddress().getDistrict());
        }
        dto.setStatus(listing.getStatus().name());

        return dto;
    }

    private OwnerBookingDto mapToOwnerBookingDto(Booking booking) {
        OwnerBookingDto dto = modelMapper.map(booking, OwnerBookingDto.class);

        dto.setNights(ChronoUnit.DAYS.between(booking.getStartDate(), booking.getEndDate()));

        User renter = booking.getRenter();
        if (renter != null) {
            dto.setRenterId(renter.getId());
            dto.setRenterName(renter.getFullName());
            dto.setRenterEmail(renter.getEmail());
            dto.setRenterPhone(renter.getPhone());
        }

        return dto;
    }

    public static class OwnerStatsDto {
        private long totalListings;
        private long activeListings;
        private long draftListings;
        private long inactiveListings;

        public long getTotalListings() {
            return totalListings;
        }

        public void setTotalListings(long totalListings) {
            this.totalListings = totalListings;
        }

        public long getActiveListings() {
            return activeListings;
        }

        public void setActiveListings(long activeListings) {
            this.activeListings = activeListings;
        }

        public long getDraftListings() {
            return draftListings;
        }

        public void setDraftListings(long draftListings) {
            this.draftListings = draftListings;
        }

        public long getInactiveListings() {
            return inactiveListings;
        }

        public void setInactiveListings(long inactiveListings) {
            this.inactiveListings = inactiveListings;
        }
    }
}
