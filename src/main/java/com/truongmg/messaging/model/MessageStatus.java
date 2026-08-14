package com.truongmg.messaging.model;

public enum MessageStatus {

    /** Save to DB; recipient is offline or not yet notified */
    SENT,

    /** Pushed to recipient's Websocket session. */
    DELIVERED

}
