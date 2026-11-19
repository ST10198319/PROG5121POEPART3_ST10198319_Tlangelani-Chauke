package com.mycompany.poepart3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class QuickChatTest {
    @TempDir
    Path tempDir;

    private Message validMessage(int number) {
        return new Message("0012345678", number, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
    }

    @Test
    void sendMessageReturnsSuccessAndCountsIt() {
        QuickChat chat = new QuickChat(tempDir.resolve("stored.json"));
        assertEquals("Message successfully sent.", chat.sentMessage(validMessage(0), QuickChat.SendChoice.SEND));
        assertEquals(1, chat.returnTotalMessages());
    }

    @Test
    void disregardMessageIsNotCounted() {
        QuickChat chat = new QuickChat(tempDir.resolve("stored.json"));
        Message discarded = new Message("0198765432", 1, "08575975889", "Hi Keegan, did you receive the payment?");
        assertEquals("Press 0 to delete the message.", chat.sentMessage(discarded, QuickChat.SendChoice.DISCARD));
        assertEquals(0, chat.returnTotalMessages());
    }

    @Test
    void storeMessageWritesJsonFile() throws IOException {
        Path file = tempDir.resolve("stored.json");
        QuickChat chat = new QuickChat(file);
        assertEquals("Message successfully stored.", chat.sentMessage(validMessage(0), QuickChat.SendChoice.STORE));
        String json = Files.readString(file);
        assertTrue(json.contains("\"messageId\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertEquals(0, chat.returnTotalMessages());
    }

    @Test
    void invalidRecipientCannotBeSent() {
        QuickChat chat = new QuickChat(tempDir.resolve("stored.json"));
        Message bad = new Message("0198765432", 1, "08575975889", "Hi Keegan, did you receive the payment?");
        assertTrue(chat.sentMessage(bad, QuickChat.SendChoice.SEND).startsWith("Cell phone number is incorrectly formatted"));
        assertEquals(0, chat.returnTotalMessages());
    }

    @Test
    void tooLongMessageCannotBeSent() {
        QuickChat chat = new QuickChat(tempDir.resolve("stored.json"));
        Message tooLong = new Message("0012345678", 0, "+27718693002", "a".repeat(251));
        assertEquals("Please enter a message of less than 250 characters.",
                chat.sentMessage(tooLong, QuickChat.SendChoice.SEND));
    }

    @Test
    void printMessagesListsSentMessages() {
        QuickChat chat = new QuickChat(tempDir.resolve("stored.json"));
        assertEquals("No messages have been sent.", chat.printMessages());
        chat.sentMessage(validMessage(0), QuickChat.SendChoice.SEND);
        assertEquals(validMessage(0).getDetails(), chat.printMessages());
    }

    @Test
    void generatedMessageIdIsTenDigits() {
        assertTrue(QuickChat.generateMessageId().matches("\\d{10}"));
    }
    @Test
    void generatedIdsAreUniqueAcrossManyMessages() {
        QuickChat chat = new QuickChat(tempDir.resolve("ids.json"));
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String id = chat.generateUniqueMessageId();
            assertTrue(id.matches("\\d{10}"));
            assertTrue(ids.add(id), "Generated message IDs must be unique.");
        }
    }

    @Test
    void arraysTrackSentStoredAndDisregardedMessages() {
        QuickChat chat = new QuickChat(tempDir.resolve("arrays.json"));
        Message sent = new Message("0012345678", 0, "+27834557896", "Did you get the cake?");
        Message stored = new Message("1012345678", 1, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
        Message discarded = new Message("2012345678", 2, "+27834484567",
                "Yohoooo, I am at your gate.");
        assertEquals("Message successfully sent.", chat.sentMessage(sent, QuickChat.SendChoice.SEND));
        assertEquals("Message successfully stored.", chat.sentMessage(stored, QuickChat.SendChoice.STORE));
        assertEquals("Press 0 to delete the message.", chat.sentMessage(discarded, QuickChat.SendChoice.DISCARD));
        assertEquals(1, chat.getSentMessageArray().length);
        assertEquals(1, chat.getStoredMessageArray().length);
        assertEquals(1, chat.getDisregardedMessageArray().length);
        assertEquals(3, chat.getMessageHashArray().length);
        assertEquals(3, chat.getMessageIdArray().length);
    }

    @Test
    void storedMessagesCanBeReloadedAndSearched() {
        Path file = tempDir.resolve("reload.json");
        QuickChat first = new QuickChat(file);
        Message secondStored = new Message("1012345678", 0, "0838884567", "+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
        assertEquals("Message successfully stored.", first.sentMessage(secondStored, QuickChat.SendChoice.STORE));
        Message fifth = new Message("5012345678", 1, "0838884567", "+27838884567",
                "Ok, I am leaving without you.");
        assertEquals("Message successfully stored.", first.sentMessage(fifth, QuickChat.SendChoice.STORE));

        QuickChat second = new QuickChat(file);
        assertEquals(2, second.getStoredMessages().size());
        assertTrue(second.searchMessageId("1012345678").contains("Where are you?"));
        assertTrue(second.searchStoredByRecipient("+27838884567").contains("Where are you?"));
        assertEquals("Where are you? You are late! I have asked you to be on time.", second.getLongestStoredMessage());
        assertTrue(second.displayStoredSenderRecipient().contains("0838884567"));
        assertTrue(second.displayStoredReport().contains("Message Hash:"));
        String recipientMessages = second.searchStoredByRecipient("+27838884567");
        assertTrue(recipientMessages.contains("Where are you?"));
        assertTrue(recipientMessages.contains("Ok, I am leaving without you."));
    }

    @Test
    void storedMessageCanBeDeletedByHashAndReportUpdates() {
        QuickChat chat = new QuickChat(tempDir.resolve("delete.json"));
        Message stored = new Message("4012345678", 0, "+27838884567",
                "Where are you? You are late! I have asked you to be on time.");
        chat.sentMessage(stored, QuickChat.SendChoice.STORE);
        assertTrue(chat.deleteStoredByHash(stored.getHash()).contains("successfully deleted"));
        assertEquals(0, chat.getStoredMessages().size());
        assertEquals("No stored messages.", chat.displayStoredReport());
    }

    @Test
    void recentMessagesReturnsOnlyRequestedNumber() {
        QuickChat chat = new QuickChat(tempDir.resolve("recent.json"));
        for (int i = 0; i < 4; i++) {
            Message m = new Message(String.format("%010d", i + 1), i,
                    "+27834557896", "Message number " + (i + 1));
            chat.sentMessage(m, QuickChat.SendChoice.SEND);
        }
        String recent = chat.getRecentMessages(2);
        assertTrue(recent.contains("Message number 3"));
        assertTrue(recent.contains("Message number 4"));
        assertFalse(recent.contains("Message number 2"));
    }

    @Test
    void screenshotMessageFourIsSentAndSearchableByItsId() {
        QuickChat chat = new QuickChat(tempDir.resolve("message-four.json"));
        Message fourth = new Message("0838884567", 0, "0838884567",
                "+27834557896", "It is dinner time!");
        assertEquals("Message successfully sent.", chat.sentMessage(fourth, QuickChat.SendChoice.SEND));
        assertTrue(chat.searchMessageId("0838884567").contains("It is dinner time!"));
        assertTrue(chat.getRecentMessages(10).contains("It is dinner time!"));
    }

}
