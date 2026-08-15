package com.truongmg.messaging.websocket;

import com.truongmg.messaging.websocket.handshake.HandShakeResponder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HandshakeResponderTest {

    @Test
    void rfc6455_officialTestVector() {
        String clientKey = "dGhlIHNhbXBsZSBub25jZQ==";
        String expectedKey = "s3pPLMBiTxaQ9kYGzzhZRbK+xOo=";

        assertThat(HandShakeResponder.computeAcceptKey(clientKey)).isEqualTo(expectedKey);
    }

    @Test
    void differentKeyProducesDifferentAccept() {
        String key1 = "dGhlIHNhbXBsZSBub25jZQ==";
        String key2 = "aGVsbG8gd29ybGQ=";

        assertThat(HandShakeResponder.computeAcceptKey(key1))
                .isNotEqualTo(HandShakeResponder.computeAcceptKey(key2));
    }

}
