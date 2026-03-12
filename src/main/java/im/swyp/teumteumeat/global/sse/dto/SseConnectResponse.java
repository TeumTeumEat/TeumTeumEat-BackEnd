package im.swyp.teumteumeat.global.sse.dto;

public record SseConnectResponse(
        String status
) implements SseResponse {
    private final static String CONNECTED = "CONNECTED";

    public static SseConnectResponse connected() {
        return new SseConnectResponse(CONNECTED);
    }

    @Override
    public String getStatus() {
        return status;
    }
}
