package com.oldshensheep.place.web.request;

import java.util.List;

public record PutPixelRequest(
        Integer x,
        Integer y,
        List<Integer> color
) {
}
