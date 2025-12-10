package com.max.rental.config;

import com.max.rental.models.entities.*;
import com.max.rental.models.enums.EnumBookingStatus;
import com.max.rental.models.enums.EnumListingStatus;
import com.max.rental.models.enums.EnumPropertyType;
import com.max.rental.models.enums.UserRole;
import com.max.rental.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class CommandLineRunnerImpl implements CommandLineRunner {

    private final ListingRepository listingRepository;
    private final PropertyTypeRepository propertyTypeRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.password:Password123}")
    private String defaultPassword;

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            log.info("База данных уже инициализирована, пропускаем");
            return;
        }

        log.info("Начало инициализации базы данных");

        log.info("Создание ролей...");
        Role guestRole = createRole(UserRole.GUEST);
        Role ownerRole = createRole(UserRole.OWNER);
        Role adminRole = createRole(UserRole.ADMIN);

        log.info("Создание типов жилья");
        PropertyType apartment = createPropertyType(EnumPropertyType.APARTMENT);
        PropertyType house = createPropertyType(EnumPropertyType.HOUSE);
        PropertyType room = createPropertyType(EnumPropertyType.ROOM);

        log.info("Создание тестовых пользователей");
        log.info("Используемый пароль для хеширования: {}", defaultPassword);

        User admin = createUser("admin@rental.ru", "Админ", "Системы",
                List.of(adminRole));

        User guest1 = createUser("guest1@test.ru", "Мария", "Иванова",
                List.of(guestRole));
        User guest2 = createUser("guest2@test.ru", "Денис", "Козлов",
                List.of(guestRole));

        User owner1 = createUser("owner1@test.ru", "Иван", "Петров",
                List.of(ownerRole));
        owner1.setOwnerRating(4.8);

        userRepository.save(owner1);

        User owner2 = createUser("owner2@test.ru", "Анна", "Смирнова",
                List.of(ownerRole));
        owner2.setOwnerRating(4.9);

        userRepository.save(owner2);

        User owner3 = createUser("owner3@test.ru", "Михаил", "Волков",
                List.of(ownerRole));
        owner3.setOwnerRating(4.0);

        userRepository.save(owner3);

        log.info("Создание объявлений");

        Listing listing1 = createListing(owner1, apartment,
                createAddress("Москва", "Центральный"),
                "Уютная студия в центре",
                "Светлая студия в 5 мин от метро. Идеально для пары. Рядом Красная площадь.",
                new BigDecimal("5500.00"), 2018, 5, 25, 1, "Без вечеринок.");
        listing1.setAverageRating(4.5);

        listingRepository.save(listing1);

        Listing listing2 = createListing(owner2, house,
                createAddress("Санкт-Петербург", "Московский"),
                "Загородный дом с баней",
                "Просторный дом для большой семьи. На участке есть баня и барбекю-зона.",
                new BigDecimal("12000.00"), 2005, 2, 1, 4, "Разрешены животные.");
        listing2.setAverageRating(5.0);

        listingRepository.save(listing2);

        Listing listing3 = createListing(owner3, room,
                createAddress("Казань", "Советский"),
                "Небольшая комната в центре",
                "Простая, но чистая комната в коммуналке. Отлично подойдет для студентов.",
                new BigDecimal("1800.00"), 1985, 9, 3, 1, "Тихий час с 22:00.");
        listing3.setAverageRating(0.0);

        listingRepository.save(listing3);

        // 5. Создание прошедших бронирований с отзывами
        log.info("Создание прошедших бронирований с отзывами");

        Booking pastBooking1 = createBooking(listing1, guest1,
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(25));
        createReviewForBooking(pastBooking1, 5,
                "Отличная квартира! Всё чисто и удобно. Хозяин очень вежливый.");

        Booking pastBooking2 = createBooking(listing1, guest2,
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(15));
        createReviewForBooking(pastBooking2, 4,
                "Немного шумно ночью из-за центрального расположения, но в целом хорошо.");

        Booking pastBooking3 = createBooking(listing2, guest1,
                LocalDate.now().minusDays(40), LocalDate.now().minusDays(35));
        createReviewForBooking(pastBooking3, 5,
                "Идеальный дом для отдыха! Баня просто супер, всё чисто.");

        // 6. Создание прошедших бронирований БЕЗ отзывов (для тестирования)
        log.info("Создание прошедших бронирований без отзывов");

        createBooking(listing3, guest1,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(7));



        // 7. Создание будущих бронирований
        log.info("Создание будущих бронирований");
        createBooking(listing1, guest1,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(6));

        // Дополнительные объявления
        log.info("Создание дополнительных объявлений");

        Listing listing4 = createListing(owner1, apartment,
                createAddress("Москва", "Южный"),
                "Современная квартира с панорамным видом",
                "Новый ЖК с развитой инфраструктурой. Панорамные окна, паркинг.",
                new BigDecimal("8500.00"), 2022, 25, 18, 2, "Запрещено курение.");
        listing4.setAverageRating(4.7);
        listingRepository.save(listing4);

        Listing listing5 = createListing(owner2, apartment,
                createAddress("Санкт-Петербург", "Центральный"),
                "Квартира у Невского проспекта",
                "Отличное расположение в самом центре. До метро 2 минуты.",
                new BigDecimal("7000.00"), 1985, 5, 3, 3, "Тихий час с 23:00.");
        listing5.setAverageRating(4.8);
        listingRepository.save(listing5);

        createBooking(listing4, guest2,
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(12));

        createBooking(listing5, guest1,
                LocalDate.now().minusDays(5), LocalDate.now().minusDays(2));

        Listing listing6 = createListing(owner3, house,
                createAddress("Казань", "Вахитовский"),
                "Загородный коттедж",
                "Тихое место за городом. Большой участок с садом.",
                new BigDecimal("15000.00"), 2010, 2, 1, 5, "Разрешены животные.");
        listing6.setAverageRating(4.9);
        listingRepository.save(listing6);

        Listing listing7 = createListing(owner1, room,
                createAddress("Екатеринбург", "Ленинский"),
                "Комната для студента",
                "Недорогая комната рядом с университетом.",
                new BigDecimal("2500.00"), 1975, 9, 5, 1, "Тихие соседи.");
        listingRepository.save(listing7);

        Listing listing8 = createListing(owner2, apartment,
                createAddress("Новосибирск", "Центральный"),
                "Просторная трёшка",
                "Квартира для большой семьи. Рядом школы и детские сады.",
                new BigDecimal("6000.00"), 2000, 10, 7, 3, "Без животных.");
        listing8.setAverageRating(4.3);
        listingRepository.save(listing8);

        Listing listing9 = createListing(owner3, apartment,
                createAddress("Москва", "Западный"),
                "Квартира в спальном районе",
                "Тихий район, хорошая транспортная доступность.",
                new BigDecimal("5000.00"), 2015, 17, 10, 2, "Без вечеринок.");
        listing9.setAverageRating(4.4);
        listingRepository.save(listing9);

        Listing listing10 = createListing(owner1, house,
                createAddress("Санкт-Петербург", "Приморский"),
                "Дом у залива",
                "Красивый вид на воду. Отличное место для отдыха.",
                new BigDecimal("20000.00"), 2018, 2, 1, 4, "Разрешены вечеринки.");
        listing10.setAverageRating(5.0);
        listingRepository.save(listing10);

        Listing listing11 = createListing(owner2, room,
                createAddress("Казань", "Приволжский"),
                "Комната в общежитии",
                "Дешёвый вариант для студентов. Все удобства.",
                new BigDecimal("1500.00"), 1980, 5, 4, 1, "Тихий час с 22:00.");
        listingRepository.save(listing11);

        Listing listing12 = createListing(owner3, apartment,
                createAddress("Екатеринбург", "Октябрьский"),
                "Однушка в новостройке",
                "Новый дом, свежий ремонт. Идеально для пары.",
                new BigDecimal("4500.00"), 2021, 14, 9, 1, "Без курения.");
        listing12.setAverageRating(4.6);
        listingRepository.save(listing12);

        Listing listing13 = createListing(owner1, apartment,
                createAddress("Новосибирск", "Заельцовский"),
                "Двушка с балконом",
                "Просторная квартира с видом на парк.",
                new BigDecimal("5500.00"), 2012, 12, 8, 2, "Без животных.");
        listing13.setAverageRating(4.5);
        listingRepository.save(listing13);

        Listing listing14 = createListing(owner2, house,
                createAddress("Москва", "Северный"),
                "Дом с участком",
                "Большой дом для семейного отдыха. Есть мангал.",
                new BigDecimal("18000.00"), 2008, 2, 1, 6, "Разрешены животные.");
        listing14.setAverageRating(4.8);
        listingRepository.save(listing14);

        Listing listing15 = createListing(owner3, apartment,
                createAddress("Санкт-Петербург", "Невский"),
                "Квартира у метро",
                "Удобное расположение. До метро 1 минута.",
                new BigDecimal("6500.00"), 1990, 9, 5, 2, "Без вечеринок.");
        listing15.setAverageRating(4.4);
        listingRepository.save(listing15);

        Listing listing16 = createListing(owner1, room,
                createAddress("Казань", "Московский"),
                "Комната в центре",
                "Маленькая, но уютная комната. Рядом вся инфраструктура.",
                new BigDecimal("2000.00"), 1978, 5, 3, 1, "Тихие соседи.");
        listingRepository.save(listing16);

        Listing listing17 = createListing(owner2, apartment,
                createAddress("Екатеринбург", "Чкаловский"),
                "Квартира-студия",
                "Современная студия в новом доме.",
                new BigDecimal("4000.00"), 2020, 16, 12, 1, "Без курения.");
        listing17.setAverageRating(4.7);
        listingRepository.save(listing17);

        Listing listing18 = createListing(owner3, house,
                createAddress("Новосибирск", "Кировский"),
                "Коттедж у леса",
                "Тихое место на природе. Идеально для отдыха.",
                new BigDecimal("16000.00"), 2015, 2, 1, 5, "Разрешены животные.");
        listing18.setAverageRating(4.9);
        listingRepository.save(listing18);

        Listing listing19 = createListing(owner1, apartment,
                createAddress("Москва", "Восточный"),
                "Трёшка на окраине",
                "Большая квартира для семьи. Тихий район.",
                new BigDecimal("7500.00"), 2005, 14, 10, 3, "Без животных.");
        listing19.setAverageRating(4.2);
        listingRepository.save(listing19);

        Listing listing20 = createListing(owner2, apartment,
                createAddress("Санкт-Петербург", "Калининский"),
                "Квартира с евроремонтом",
                "Стильный ремонт, вся техника. Готова к заселению.",
                new BigDecimal("8000.00"), 2019, 20, 15, 2, "Без вечеринок.");
        listing20.setAverageRating(4.8);
        listingRepository.save(listing20);

        Listing listing21 = createListing(owner3, room,
                createAddress("Казань", "Советский"),
                "Комната в коммуналке",
                "Недорогой вариант. Чистая комната.",
                new BigDecimal("1800.00"), 1982, 5, 2, 1, "Тихий час с 22:00.");
        listingRepository.save(listing21);

        Listing listing22 = createListing(owner1, apartment,
                createAddress("Екатеринбург", "Верх-Исетский"),
                "Двушка с хорошим ремонтом",
                "Свежий ремонт, новая мебель. Всё включено.",
                new BigDecimal("5000.00"), 2017, 12, 7, 2, "Без курения.");
        listing22.setAverageRating(4.6);
        listingRepository.save(listing22);

        Listing listing23 = createListing(owner2, house,
                createAddress("Новосибирск", "Советский"),
                "Дом для большой компании",
                "Просторный дом. Можно провести мероприятие.",
                new BigDecimal("22000.00"), 2012, 2, 1, 7, "Разрешены вечеринки.");
        listing23.setAverageRating(4.7);
        listingRepository.save(listing23);

        // Дополнительные бронирования для популярности
        createBooking(listing2, guest2, LocalDate.now().plusDays(10), LocalDate.now().plusDays(15));
        createBooking(listing5, guest1, LocalDate.now().plusDays(7), LocalDate.now().plusDays(10));
        createBooking(listing10, guest2, LocalDate.now().plusDays(20), LocalDate.now().plusDays(25));

        log.info("Инициализация завершена успешно!");
        log.info("Тестовые пользователи (пароль для всех: {}):", defaultPassword);
        log.info("  - admin@rental.ru (ADMIN)");
        log.info("  - guest1@test.ru (GUEST)");
        log.info("  - guest2@test.ru (GUEST)");
        log.info("  - owner1@test.ru (OWNER)");
        log.info("  - owner2@test.ru (OWNER)");
        log.info("  - owner3@test.ru (OWNER)");
        log.info("Всего объявлений: {}", listingRepository.count());
    }


    private Role createRole(UserRole userRole) {
        Role role = new Role(userRole);
        return roleRepository.save(role);
    }

    private User createUser(String email, String firstName, String lastName, List<Role> roles) {
        User user = new User();
        user.setEmail(email);

        String encodedPassword = passwordEncoder.encode(defaultPassword);
        log.debug("Хеширование пароля для {}: {} -> {}", email, defaultPassword, encodedPassword.substring(0, 20));
        user.setPasswordHash(encodedPassword);

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setOwnerRating(0.0);
        return userRepository.save(user);
    }

    private PropertyType createPropertyType(EnumPropertyType enumType) {
        PropertyType pt = new PropertyType();
        pt.setType(enumType.getCode());
        return propertyTypeRepository.save(pt);
    }

    private Address createAddress(String city, String district) {
        Address address = new Address();
        address.setCity(city);
        address.setDistrict(district);
        return address;
    }

    private Listing createListing(User owner, PropertyType type, Address address,
                                  String title, String description, BigDecimal price,
                                  int year, int totalFloors, int floor, int roomCount, String rules) {
        Listing listing = new Listing();
        listing.setOwner(owner);
        listing.setPropertyType(type);
        listing.setAddress(address);
        listing.setTitle(title);
        listing.setDescription(description);
        listing.setPricePerNight(price);
        listing.setConstructionYear(year);
        listing.setTotalFloors(totalFloors);
        listing.setFloor(floor);
        listing.setRoomCount(roomCount);
        listing.setRules(rules);
        listing.setStatus(EnumListingStatus.ACTIVE);
        listing.setCreatedAt(LocalDateTime.now());
        return listing;
    }

    private Booking createBooking(Listing listing, User renter, LocalDate start, LocalDate end) {
        Booking booking = new Booking();
        booking.setListing(listing);
        booking.setRenter(renter);
        booking.setStartDate(start);
        booking.setEndDate(end);
        booking.setStatus(EnumBookingStatus.CONFIRMED);
        long nights = java.time.temporal.ChronoUnit.DAYS.between(start, end);
        booking.setTotalPrice(listing.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        return bookingRepository.save(booking);
    }

    private void createReviewForBooking(Booking booking, int rating, String comment) {
        Review review = new Review();
        review.setBooking(booking);
        review.setListing(booking.getListing());
        review.setGuest(booking.getRenter());
        review.setRating(rating);
        review.setComment(comment);
        reviewRepository.save(review);
    }
}