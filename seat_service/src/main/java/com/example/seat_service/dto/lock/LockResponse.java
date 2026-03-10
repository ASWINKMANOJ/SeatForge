package com.example.seat_service.dto.lock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockResponse {
    private List<Long> eventSeatStatusIds;  // needed to unlock later
    private Instant lockExpiresAt;
}
