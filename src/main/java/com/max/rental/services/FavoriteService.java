package com.max.rental.services;

import com.max.rental.dto.favorite.FavoriteListingDto;
import com.max.rental.models.entities.Favorite;
import com.max.rental.models.entities.Listing;
import com.max.rental.repositories.FavoriteRepository;
import com.max.rental.repositories.ListingRepository;
import com.max.rental.security.CurrentUserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final CurrentUserService currentUserService;
    private final ModelMapper modelMapper;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           ListingRepository listingRepository,
                           CurrentUserService currentUserService,
                           ModelMapper modelMapper) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
        this.currentUserService = currentUserService;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public void addToFavorites(Long listingId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        log.info("Пользователь {} добавляет объявление {} в избранное", currentUserId, listingId);

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));

        if (listing.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Вы не можете добавить собственное объявление в избранное.");
        }

        if (favoriteRepository.existsByRenterIdAndListingId(currentUserId, listingId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Объявление уже в избранном");
        }

        Favorite favorite = new Favorite(currentUserId, listingId);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFromFavorites(Long listingId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        log.info("Пользователь {} удаляет объявление {} из избранного", currentUserId, listingId);

        if (!favoriteRepository.existsByRenterIdAndListingId(currentUserId, listingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено в избранном");
        }

        favoriteRepository.deleteByRenterIdAndListingId(currentUserId, listingId);
    }

    @Transactional(readOnly = true)
    public List<FavoriteListingDto> getFavorites() {
        Long currentUserId = currentUserService.getCurrentUserId();
        List<Favorite> favorites = favoriteRepository.findAllByRenterId(currentUserId);

        return favorites.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(Long listingId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        return favoriteRepository.existsByRenterIdAndListingId(currentUserId, listingId);
    }


    @Transactional
    public boolean toggleFavorite(Long listingId) {
        Long currentUserId = currentUserService.getCurrentUserId();

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Объявление не найдено"));

        if (listing.getOwner().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Вы не можете добавить собственное объявление в избранное.");
        }

        if (favoriteRepository.existsByRenterIdAndListingId(currentUserId, listingId)) {
            favoriteRepository.deleteByRenterIdAndListingId(currentUserId, listingId);
            log.info("Объявление {} удалено из избранного пользователя {}", listingId, currentUserId);
            return false;
        } else {
            Favorite favorite = new Favorite(currentUserId, listingId);
            favoriteRepository.save(favorite);
            log.info("Объявление {} добавлено в избранное пользователя {}", listingId, currentUserId);
            return true;
        }
    }

    private FavoriteListingDto mapToDto(Favorite favorite) {
        FavoriteListingDto dto = new FavoriteListingDto();
        dto.setListingId(favorite.getListingId());
        dto.setAddedAt(favorite.getCreatedAt());

        listingRepository.findById(favorite.getListingId()).ifPresent(listing -> {
            FavoriteListingDto listingDto = modelMapper.map(listing, FavoriteListingDto.class);
            dto.setTitle(listingDto.getTitle());
            dto.setDescription(listing.getDescription());
            dto.setPricePerNight(listing.getPricePerNight());
            dto.setAverageRating(listing.getAverageRating());

            if (listing.getAddress() != null) {
                dto.setCity(listing.getAddress().getCity());
                dto.setDistrict(listing.getAddress().getDistrict());
            }
            if (listing.getPropertyType() != null) {
                dto.setPropertyType(listing.getPropertyType().getType());
            }
        });

        return dto;
    }
}
