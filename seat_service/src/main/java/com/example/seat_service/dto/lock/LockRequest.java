package com.example.seat_service.dto.lock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockRequest {
    private Long eventId;
    private List<Long> seatIds;
}
