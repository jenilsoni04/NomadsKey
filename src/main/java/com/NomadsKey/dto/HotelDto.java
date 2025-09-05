package com.NomadsKey.dto;

import com.NomadsKey.entity.HotelContactInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class HotelDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    private String name;
    private String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private Boolean active;
}
