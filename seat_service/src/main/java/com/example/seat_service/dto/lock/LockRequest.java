package com.example.seat_service.dto.lock;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockRequest {
    @NotNull(message = "Event id is required")
    private Long eventId;
    @NotEmpty(message = "Seat id is required")
    private List<Long> seatIds;
}
