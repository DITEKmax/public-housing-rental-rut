package com.max.rental.services;

import com.max.rental.config.RedisCacheConfig;
import com.max.rental.dto.booking.BookingHistoryDto;
import com.max.rental.dto.booking.BookingRequestDto;
import com.max.rental.dto.booking.PriceCalculationDto;
import com.max.rental.models.entities.Booking;
import com.max.rental.models.entities.Listing;
import com.max.rental.models.entities.User;
import com.max.rental.models.enums.EnumBookingStatus;
import com.max.rental.models.enums.EnumListingStatus;
import com.max.rental.repositories.BookingRepository;
import com.max.rental.repositories.ListingRepository;
import com.max.rental.repositories.ReviewRepository;
import com.max.rental.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05"); // 5% сервисный сбор

    private final BookingRepository bookingRepository;
    private final ListingRepository listingRepository;
    private final ReviewRepository reviewRepository;
    private final CurrentUserService currentUserService;


    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.LISTING_SEARCH_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.POPULAR_CITIES_CACHE, allEntries = true)
    })
    public void createBooking(BookingRequestDto dto) {
        Long currentUserId = currentUserService.getCurrentUserId();
        User currentUser = currentUserService.getCurrentUser();

        log.info("Пользователь {} создает бронирование для объявления {}", currentUserId, dto.getListingId());

        Listing listing = listingRepository.findById(dto.getListingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));

        if (listing.getStatus() != EnumListingStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Это объявление недоступно для бронирования");
        }

        if (listing.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя забронировать собственное жильё");
        }

        LocalDate today = LocalDate.now();
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите даты бронирования");
        }
        if (dto.getStartDate().isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата заезда не может быть в прошлом");
        }
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата выезда должна быть позже даты заезда");
        }

        List<Booking> existingBookings = bookingRepository.findAllByListingId(dto.getListingId());
        for (Booking existing : existingBookings) {
            if (existing.getStatus() == EnumBookingStatus.CANCELLED) {
                continue;
            }
            if (!(dto.getEndDate().isBefore(existing.getStartDate()) ||
                    dto.getStartDate().isAfter(existing.getEndDate()) ||
                    dto.getEndDate().isEqual(existing.getStartDate()) ||
                    dto.getStartDate().isEqual(existing.getEndDate()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Выбранные даты уже заняты. Пожалуйста, выберите другие даты.");
            }
        }

        long nights = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
        BigDecimal totalPrice = listing.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setListing(listing);
        booking.setRenter(currentUser);
        booking.setStartDate(dto.getStartDate());
        booking.setEndDate(dto.getEndDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(EnumBookingStatus.CONFIRMED);

        bookingRepository.save(booking);
        log.info("Бронирование создано: {} для пользователя {}", booking.getId(), currentUser.getEmail());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = RedisCacheConfig.LISTING_SEARCH_CACHE, allEntries = true),
            @CacheEvict(value = RedisCacheConfig.POPULAR_CITIES_CACHE, allEntries = true)
    })
    public void cancelBooking(Long bookingId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        log.info("Пользователь {} отменяет бронирование {}", currentUserId, bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Бронирование не найдено"));

        if (!booking.getRenter().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Вы не можете отменить чужое бронирование");
        }

        if (booking.getStatus() == EnumBookingStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Бронирование уже отменено");
        }

        if (booking.getStartDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Нельзя отменить бронирование после даты заезда");
        }

        booking.setStatus(EnumBookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Бронирование {} успешно отменено", bookingId);
    }

    @Transactional(readOnly = true)
    public List<BookingHistoryDto> getBookingHistory() {
        Long currentUserId = currentUserService.getCurrentUserId();
        List<Booking> bookings = bookingRepository.findAllByRenterId(currentUserId);
        LocalDate today = LocalDate.now();

        return bookings.stream()
                .sorted((b1, b2) -> {
                    boolean b1IsUpcoming = b1.getStartDate().isAfter(today) || b1.getStartDate().isEqual(today);
                    boolean b2IsUpcoming = b2.getStartDate().isAfter(today) || b2.getStartDate().isEqual(today);

                    if (b1IsUpcoming == b2IsUpcoming) {
                        if (b1IsUpcoming) {
                            return b1.getStartDate().compareTo(b2.getStartDate());
                        } else {
                            return b2.getEndDate().compareTo(b1.getEndDate());
                        }
                    }
                    return b1IsUpcoming ? -1 : 1;
                })
                .map(this::mapToHistoryDto)
                .collect(Collectors.toList());
    }

    private BookingHistoryDto mapToHistoryDto(Booking booking) {
        BookingHistoryDto dto = new BookingHistoryDto();
        dto.setId(booking.getId());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());

        Listing listing = booking.getListing();
        dto.setListingId(listing.getId());
        dto.setListingTitle(listing.getTitle());
        if (listing.getAddress() != null) {
            dto.setCity(listing.getAddress().getCity());
        }

        boolean hasReview = reviewRepository.findByBookingId(booking.getId()).isPresent();
        dto.setHasReview(hasReview);

        boolean canLeaveReview = !hasReview
                && booking.getStatus() == EnumBookingStatus.CONFIRMED
                && booking.getEndDate().isBefore(LocalDate.now());
        dto.setCanLeaveReview(canLeaveReview);

        return dto;
    }

    @Transactional(readOnly = true)
    public PriceCalculationDto calculatePrice(Long listingId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Укажите даты бронирования");
        }
        if (!endDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата выезда должна быть позже даты заезда");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));

        long nights = ChronoUnit.DAYS.between(startDate, endDate);

        BigDecimal pricePerNight = listing.getPricePerNight();
        BigDecimal subtotal = pricePerNight.multiply(BigDecimal.valueOf(nights));
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(serviceFee);

        return new PriceCalculationDto(nights, pricePerNight, subtotal, serviceFee, total);
    }
}