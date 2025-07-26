package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.BookingDto;
import com.airbnb.airbnb.dto.BookingRequest;
import com.airbnb.airbnb.dto.GuestDto;
import com.airbnb.airbnb.entity.*;
import com.airbnb.airbnb.entity.enums.BookingStatus;
import com.airbnb.airbnb.exception.ResourceNotFoundException;
import com.airbnb.airbnb.exception.UnAuthorisedException;
import com.airbnb.airbnb.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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
        for(Inventory inventory1:inventory)
        {
            inventory1.setReservedCount(inventory1.getReservedCount()+bookingRequest.getRoomsCount());
        }
        inventoryRepository.saveAll(inventory);


        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkindate(bookingRequest.getCheckInDate())
                .checkoutdate(bookingRequest.getCheckOutDate())
                .user(getCurrentUser())
                .roomsCount(bookingRequest.getRoomsCount())
                .amount(BigDecimal.TEN)
                .build();


        booking = bookingRepository.save(booking);
        return modelMapper.map(booking, BookingDto.class);
    }

    @Override
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
    public Boolean hasbookingexpired(Booking booking) {
        return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

    public User getCurrentUser() {

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
