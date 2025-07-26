package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.RoomDto;
import com.airbnb.airbnb.entity.Hotel;
import com.airbnb.airbnb.entity.Room;
import com.airbnb.airbnb.entity.User;
import com.airbnb.airbnb.exception.ResourceNotFoundException;
import com.airbnb.airbnb.exception.UnAuthorisedException;
import com.airbnb.airbnb.repository.HotelRepository;
import com.airbnb.airbnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceimpl implements RoomService{

    private final RoomRepository roomRepository;
    private final ModelMapper mapper;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new roomm"+hotelId);
        Hotel hotel=hotelRepository.findById(hotelId)
                        .orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+hotelId));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this hotel with id"+hotelId);
        }
        Room room=mapper.map(roomDto,Room.class);
        room.setHotel(hotel);
        room= roomRepository.save(room);
        if (hotel.getActive()) {
            inventoryService.initializeRoomForAYear(room);
        }

        return mapper.map(room,RoomDto.class);

    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with ID: {}", hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this hotel with id"+hotelId);
        }
        return hotel.getRooms()
                .stream()
                .map((element) -> mapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getroombyid(Long id) {
        log.info("Getting the room with ID: {}", id);
        Room room = roomRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+id));
        return mapper.map(room, RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteroombyid(Long roomId) {
        log.info("Deleting the room with ID: {}", roomId);
        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: "+roomId));
        inventoryService.deleteallinventories(room);
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this room with id"+roomId);
        }
        roomRepository.deleteById(roomId);
    }
}
