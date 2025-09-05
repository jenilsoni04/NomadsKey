package com.NomadsKey.service;

import com.NomadsKey.Strategy.PricingService;
import com.NomadsKey.dto.BookingDto;
import com.NomadsKey.dto.BookingRequest;
import com.NomadsKey.dto.HotelReportDto;
import com.NomadsKey.entity.*;
import com.NomadsKey.repository.*;
import com.NomadsKey.entity.enums.BookingStatus;
import com.NomadsKey.exception.ResourceNotFoundException;
import com.NomadsKey.exception.UnAuthorisedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.NomadsKey.utils.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService{
    private final HotelRepository hotelRepository;
    private final RoomRepository RoomRepository;
    private final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final InventoryRepository inventoryRepository;
    private final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initialising booking for hotel : {}, room: {}, date {}-{}", bookingRequest.getHotelId(),
                bookingRequest.getRoomId(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
        Hotel hotel=hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+bookingRequest.getHotelId()));
        Room room= RoomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()->new ResourceNotFoundException("Room not found with ID"+bookingRequest.getRoomId()));
        List<Inventory> inventory=inventoryRepository.findandlockavailableInventory(room.getId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());
        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate())+1;
        if(inventory.size()!=daysCount)
        {
            throw new IllegalStateException("Room is not available anymore");
        }
        inventoryRepository.initBooking(room.getId(), bookingRequest.getCheckInDate(),
                bookingRequest.getCheckOutDate(), bookingRequest.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventory);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));

        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkindate(bookingRequest.getCheckInDate())
                .checkoutdate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<Long> guestIdList) {
        log.info("Adding guests to booking with ID: {}", bookingId);
        Booking booking = bookingRepository
                .findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: "+bookingId));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        if(hasbookingexpired(booking))
        {
            throw new IllegalStateException("Booking has expired");
        }
        if(booking.getBookingStatus()!=BookingStatus.RESERVED)
        {
            throw new IllegalStateException("Booking is not in reserved state");
        }
        for (Long guestId: guestIdList) {
            Guest guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: "+guestId));
            booking.getGuest().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking=bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);

    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        if (hasbookingexpired(booking)) {
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(booking,
                frontendUrl+"/payments/success", frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);

        return sessionUrl;
    }
    @Override
    @Transactional
    public void capturePayment(Event event) {

        if("checkout.session.completed".equals(event.getType())){
            Session session = retrieveSessionFromEvent(event);
            if(session == null|| session.getId()==null)return;

            String sessionId = session.getId();
            Booking booking = bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(
                    ()-> new ResourceNotFoundException("Booking not found for session Id :"+sessionId)
            );

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),booking.getCheckindate()
                    ,booking.getCheckoutdate(),booking.getRoomsCount());
            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckindate()
                    ,booking.getCheckoutdate(),booking.getRoomsCount());

            log.info("Successfully confirmed the booking for Booking Id : {}",booking.getId());
        }
        else {
            log.warn("Unhandled event Type : {}",event.getType());
        }

    }
    private Session retrieveSessionFromEvent(Event event) {
        log.info("inside  retrieveSessionFromEvent");
        try {

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            if (deserializer.getObject().isPresent()) {
                return (Session) deserializer.getObject().get();
            } else {
                String rawJson = event.getData().getObject().toJson();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(rawJson);
                String sessionId = jsonNode.get("id").asText();

                return Session.retrieve(sessionId);
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to retrieve session data");
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new ResourceNotFoundException("Booking not found with id: "+bookingId)
        );
        User user = getCurrentUser();
        if (!user.equals(booking.getUser())) {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be cancelled");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckindate(),
                booking.getCheckoutdate(), booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(), booking.getCheckindate(),
                booking.getCheckoutdate(), booking.getRoomsCount());
        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundParams);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BookingStatus getBookingStatus(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found with ID: "+bookingId));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        return booking.getBookingStatus();
    }

    @Override
    public List<BookingDto> getAllBookingsByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Getting all booking for the hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not " +
                "found with ID: "+hotelId));
        User user = getCurrentUser();

        log.info("Generating report for hotel with ID: {}", hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of hotel with id: "+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel, startDateTime, endDateTime);

        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBookings = bookings.stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO :
                totalRevenueOfConfirmedBookings.divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings, totalRevenueOfConfirmedBookings, avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream().
                map((element) -> modelMapper.map(element, BookingDto.class))
                .collect(Collectors.toList());
    }
    public Boolean hasbookingexpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }


}
