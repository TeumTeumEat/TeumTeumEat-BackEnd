package im.swyp.teumteumeat.global.common;

import lombok.Builder;

@Builder
public record CreatedResponse(
        Long id
) {
    public static CreatedResponse from(Long id) {
        return new CreatedResponse(id);
    }
}
