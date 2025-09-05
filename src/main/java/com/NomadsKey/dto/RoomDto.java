package com.NomadsKey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    private String type;
    private BigDecimal baseprice;
    private String[] photos;
    private String[] amenities;
    private Integer totalcount;
    private Integer capacity;
}
