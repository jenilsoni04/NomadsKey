package com.airbnb.airbnb.service;

import com.airbnb.airbnb.Strategy.PricingService;
import com.airbnb.airbnb.dto.BookingDto;
import com.airbnb.airbnb.dto.BookingRequest;
import com.airbnb.airbnb.dto.GuestDto;
import com.airbnb.airbnb.entity.*;
import com.airbnb.airbnb.entity.enums.BookingStatus;
import com.airbnb.airbnb.exception.ResourceNotFoundException;
import com.airbnb.airbnb.exception.UnAuthorisedException;
import com.airbnb.airbnb.repository.*;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

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
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
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
        for(GuestDto guestDto:guestDtoList)
        {
            Guest guest=modelMapper.map(guestDto,Guest.class);
            guest.setUser(getCurrentUser());
            guest=guestRepository.save(guest);
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


//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
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
    public String getBookingStatus(Long bookingId) {
        Booking booking=bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found with ID: "+bookingId));
        User user=getCurrentUser();
        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorisedException("Booking does not belong to this user with id: "+user.getId());
        }
        return booking.getBookingStatus().name();
    }

    public Boolean hasbookingexpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
