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
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    public ProfileService(UserRepository userRepository,
                          BookingRepository bookingRepository,
                          FavoriteRepository favoriteRepository,
                          CurrentUserService currentUserService,
                          ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.favoriteRepository = favoriteRepository;
        this.currentUserService = currentUserService;
        this.modelMapper = modelMapper;
    }

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
        ProfileDto dto = modelMapper.map(user, ProfileDto.class);

        String role = user.getRoles().isEmpty() ? "GUEST" :
                user.getRoles().get(0).getName().name();
        dto.setRole(role);

        dto.setTotalBookings(bookingRepository.findAllByRenterId(user.getId()).size());
        dto.setFavoritesCount(favoriteRepository.countByRenterId(user.getId()));

        return dto;
    }

    private BookingHistoryDto mapToBookingHistoryDto(Booking booking) {
        BookingHistoryDto dto = modelMapper.map(booking, BookingHistoryDto.class);

        Listing listing = booking.getListing();
        dto.setListingTitle(listing.getTitle());
        if (listing.getAddress() != null) {
            dto.setCity(listing.getAddress().getCity());
        }

        return dto;
    }
}
