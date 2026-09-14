package com.vahan.assignment.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies immutable message construction and protocol invariants.
 */
class MessageTest {

    @Test
    void createsDataMessage() {
        Message message = Message.data("hello");

        assertEquals(Message.Type.DATA, message.type());
        assertEquals("hello", message.content());
    }

    @Test
    void createsStopMessageWithoutContent() {
        Message message = Message.stop();

        assertTrue(message.isStop());
        assertEquals("", message.content());
    }

    @Test
    void rejectsStopMessageWithContent() {
        assertThrows(IllegalArgumentException.class,
                () -> new Message(Message.Type.STOP, "unexpected"));
    }
}
