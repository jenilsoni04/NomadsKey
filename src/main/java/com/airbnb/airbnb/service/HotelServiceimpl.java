package com.airbnb.airbnb.service;

import com.airbnb.airbnb.dto.*;
import com.airbnb.airbnb.entity.Hotel;
import com.airbnb.airbnb.entity.Room;
import com.airbnb.airbnb.entity.User;
import com.airbnb.airbnb.exception.ResourceNotFoundException;
import com.airbnb.airbnb.exception.UnAuthorisedException;
import com.airbnb.airbnb.repository.HotelMinPriceRepository;
import com.airbnb.airbnb.repository.HotelRepository;
import com.airbnb.airbnb.repository.InventoryRepository;
import com.airbnb.airbnb.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.airbnb.airbnb.utils.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceimpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper mapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;
    private final ModelMapper modelMapper;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    public HotelDto creatennewhotel(HotelDto hotelDto) {
        log.info("Creating new hotel with name"+hotelDto.getName());
        Hotel hotel=mapper.map(hotelDto,Hotel.class);
        hotel.setActive(false);
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);
        hotel=hotelRepository.save(hotel);
        log.info("Created a new hotel with id{}",hotelDto.getId());
        return mapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto gethotelbyid(Long id) {
        log.info("Getting the hotel with Id : {}"+id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+id));
        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this hotel with id"+id);
        }
        return mapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updatehotelbyid(Long id, HotelDto hotelDto) {
        log.info("Updating the hotel with Id : {}"+id);
        Hotel hotel=hotelRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+id));

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this hotel with id"+id);
        }
         Hotel savehotel=mapper.map(hotelDto,Hotel.class);
         Hotel savedhotel=hotelRepository.save(savehotel);
         return mapper.map(savedhotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deletehotelbyid(Long id) {
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+id));
        hotelMinPriceRepository.deleteByHotelId(id);
        hotelRepository.deleteById(id);

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this hotel with id"+id);
        }

        for(Room room: hotel.getRooms()) {
            inventoryService.deleteallinventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating the hotel with Id : {}"+hotelId);
        Hotel hotel=hotelRepository.findById(hotelId).orElseThrow(()->new ResourceNotFoundException("Hotel not found with ID"+hotelId));

        User user=(User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorisedException("This user doesn't own this hotel with id"+hotelId);
        }
    hotel.setActive(true);
        for(Room room: hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId, HotelInfoRequestDto hotelInfoRequestDto) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: "+hotelId));

        long daysCount = ChronoUnit.DAYS.between(hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate())+1;

        List<RoomPriceDto> roomPriceDtoList = inventoryRepository.findRoomAveragePrice(hotelId,
                hotelInfoRequestDto.getStartDate(), hotelInfoRequestDto.getEndDate(),
                hotelInfoRequestDto.getRoomsCount(), daysCount);

        List<RoomPriceResponseDto> rooms = roomPriceDtoList.stream()
                .map(roomPriceDto -> {
                    RoomPriceResponseDto roomPriceResponseDto = modelMapper.map(roomPriceDto.getRoom(),
                            RoomPriceResponseDto.class);
                    roomPriceResponseDto.setPrice(roomPriceDto.getPrice());
                    return roomPriceResponseDto;
                })
                .collect(Collectors.toList());

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        User user = getCurrentUser();
        log.info("Getting all hotels for the admin user with ID: {}", user.getId());
        List<Hotel> hotels = hotelRepository.findByOwner(user);

        return hotels
                .stream()
                .map((element) -> modelMapper.map(element, HotelDto.class))
                .collect(Collectors.toList());
    }
}

