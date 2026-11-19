package com.mycompany.poepart3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MessageTest {
    private static final String ID = "0012345678";
    private static final String TEXT1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String TEXT2 = "Hi Keegan, did you receive the payment?";

    private final Message message1 = new Message(ID, 0, "+27718693002", TEXT1);
    private final Message message2 = new Message("0198765432", 1, "08575975889", TEXT2);

    @Test
    void messageUnder250CharactersIsReadyToSend() {
        assertEquals("Message ready to send.", message1.checkMessageLength());
    }

    @Test
    void messageOver250CharactersReportsTheExcess() {
        Message tooLong = new Message(ID, 0, "+27718693002", "a".repeat(255));
        assertEquals("Message exceeds 250 characters by 5; please reduce the size.", tooLong.checkMessageLength());
    }

    @Test
    void recipientCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    void recipientWithoutInternationalCodeFails() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. "
                + "Please correct the number and try again.", message2.checkRecipientCell());
    }

    @Test
    void messageHashForTestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
    }

    @Test
    void messageHashesForRemainingMessagesInLoop() {
        String[] ids = {ID, "0198765432", "0299999999", "0312345678"};
        String[] texts = {TEXT1, TEXT2, "Hi, thanks!", "Where are you? Call me."};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT", "02:2:HITHANKS", "03:3:WHEREME"};
        for (int i = 0; i < texts.length; i++) {
            Message m = new Message(ids[i], i, "+27718693002", texts[i]);
            assertEquals(expected[i], m.createMessageHash());
        }
    }

    @Test
    void messageIdIsCreatedAndWithinTenCharacters() {
        assertTrue(message1.checkMessageID());
        assertEquals("Message ID generated: " + ID, message1.describeMessageId());
        assertFalse(new Message("12345678901", 0, "+27718693002", TEXT1).checkMessageID());
    }

    @Test
    void detailsAreInTheRequiredOrder() {
        assertEquals("Message ID: " + ID + "\nMessage Hash: 00:0:HITONIGHT\nRecipient: +27718693002\nMessage: " + TEXT1,
                message1.getDetails());
    }
}
