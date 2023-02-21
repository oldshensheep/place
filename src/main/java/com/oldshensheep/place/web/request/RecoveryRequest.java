package com.oldshensheep.place.web.request;

import java.time.Instant;

public record RecoveryRequest(
        Instant start,
        Instant end
) {
}