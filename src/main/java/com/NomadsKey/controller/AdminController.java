package com.NomadsKey.controller;

import com.NomadsKey.advice.ApiResponse;
import com.NomadsKey.service.AdminService;
import com.NomadsKey.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final HotelService hotelService;
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/approve-hotel-manager/{userId}")
    public ResponseEntity<ApiResponse<String>> approveHotelManager(@PathVariable Long userId) {
        adminService.approveHotelManager(userId);
        return ResponseEntity.ok(new ApiResponse<>("Hotel manager approved successfully"));
    }
    @PatchMapping("/{hotelid}/activate")
    @Operation(summary = "Activate a hotel", tags = {"Admin Hotel"})
    public ResponseEntity<Void> activatehotel(@PathVariable Long hotelid)
    {
        hotelService.activateHotel(hotelid);
        return ResponseEntity.noContent().build();
    }

}

