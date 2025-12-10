package com.max.rental.services;

import com.max.rental.dto.booking.BookingHistoryDto;
import com.max.rental.dto.profile.ProfileDto;
import com.max.rental.dto.profile.ProfileUpdateDto;
import com.max.rental.models.entities.Booking;
import com.max.rental.models.entities.Listing;
import com.max.rental.models.entities.User;
import com.max.rental.repositories.BookingRepository;
import com.max.rental.repositories.FavoriteRepository;
import com.max.rental.repositories.UserRepository;
import com.max.rental.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public ProfileDto getProfile() {
        User user = currentUserService.getCurrentUser();
        return mapToProfileDto(user);
    }

    @Transactional
    public ProfileDto updateProfile(ProfileUpdateDto updateDto) {
        User user = currentUserService.getCurrentUser();
        log.info("Пользователь {} обновляет профиль", user.getEmail());

        if (updateDto.getFirstName() != null && !updateDto.getFirstName().trim().isEmpty()) {
            user.setFirstName(updateDto.getFirstName().trim());
        }

        if (updateDto.getLastName() != null && !updateDto.getLastName().trim().isEmpty()) {
            user.setLastName(updateDto.getLastName().trim());
        }

        if (updateDto.getPhone() != null) {
            user.setPhone(updateDto.getPhone().trim());
        }

        User savedUser = userRepository.save(user);
        return mapToProfileDto(savedUser);
    }


    @Transactional(readOnly = true)
    public List<BookingHistoryDto> getBookingHistory() {
        Long currentUserId = currentUserService.getCurrentUserId();
        List<Booking> bookings = bookingRepository.findAllByRenterId(currentUserId);

        return bookings.stream()
                .map(this::mapToBookingHistoryDto)
                .collect(Collectors.toList());
    }

    private ProfileDto mapToProfileDto(User user) {
        ProfileDto dto = new ProfileDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());

        String role = user.getRoles().isEmpty() ? "GUEST" :
                user.getRoles().get(0).getName().name();
        dto.setRole(role);

        dto.setOwnerRating(user.getOwnerRating());
        dto.setCreatedAt(user.getCreatedAt());

        dto.setTotalBookings(bookingRepository.findAllByRenterId(user.getId()).size());
        dto.setFavoritesCount(favoriteRepository.countByRenterId(user.getId()));

        return dto;
    }

    private BookingHistoryDto mapToBookingHistoryDto(Booking booking) {
        BookingHistoryDto dto = new BookingHistoryDto();
        dto.setId(booking.getId());
        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus());

        Listing listing = booking.getListing();
        dto.setListingTitle(listing.getTitle());
        if (listing.getAddress() != null) {
            dto.setCity(listing.getAddress().getCity());
        }

        return dto;
    }
}