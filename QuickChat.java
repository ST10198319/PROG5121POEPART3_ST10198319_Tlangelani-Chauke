/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.poepart3;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Coordinates message validation, arrays, reporting and JSON persistence. */
public class QuickChat {
    public enum SendChoice { SEND, DISCARD, STORE }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> GENERATED_IDS = new HashSet<>();
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{([^{}]*)\\}", Pattern.DOTALL);
    private static final Pattern JSON_NUMBER = Pattern.compile("\\\"messageNumber\\\"\\s*:\\s*(\\d+)");
    private static final Pattern JSON_FIELD = Pattern.compile("\"(messageId|messageHash|sender|recipient|message)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"", Pattern.DOTALL);

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> disregardedMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();
    private final Set<String> usedIds = new HashSet<>();
    private final Path jsonFile;
    private int nextMessageNumber = 0;

    public QuickChat() { this(Path.of("storedMessages.json")); }

    public QuickChat(Path jsonFile) {
        this.jsonFile = jsonFile;
        loadStoredMessages();
    }

    /** Generates a ten-digit ID and checks it against IDs already used by this chat.
     * @return  */
    public synchronized String generateUniqueMessageId() {
        String id;
        do {
            id = generateMessageId();
        } while (!usedIds.add(id));
        return id;
    }

    /** Kept for compatibility with existing tests and callers.
     * @return  */
    public static synchronized String generateMessageId() {
        String id;
        do {
            id = String.format("%010d", RANDOM.nextLong(1_000_000_000L, 10_000_000_000L));
        } while (!GENERATED_IDS.add(id));
        return id;
    }

    public Message createMessage(int messageNumber, String recipient, String body) {
        String id = generateUniqueMessageId();
        nextMessageNumber = Math.max(nextMessageNumber, messageNumber + 1);
        return new Message(id, messageNumber, recipient, body);
    }

    public Message createMessage(String recipient, String body) {
        return createMessage(nextMessageNumber++, recipient, body);
    }

    public Message createMessage(String sender, String recipient, String body) {
        return createMessage(nextMessageNumber++, sender, recipient, body);
    }

    public Message createMessage(int messageNumber, String sender, String recipient, String body) {
        String id = generateUniqueMessageId();
        nextMessageNumber = Math.max(nextMessageNumber, messageNumber + 1);
        return new Message(id, messageNumber, sender, recipient, body);
    }

    public String sentMessage(Message message, SendChoice choice) {
        if (message == null || choice == null) return "Message and action are required.";
        if (choice == SendChoice.DISCARD) {
            disregardedMessages.add(message);
            usedIds.add(message.getMessageId());
            return "Press 0 to delete the message.";
        }
        if (!message.checkMessageID()) return "Message ID must contain exactly ten digits.";
        if (!message.isRecipientValid()) return message.checkRecipientCell();
        if (message.getText().isBlank()) return "Please enter a message.";
        if (!message.isLengthValid()) return "Please enter a message of less than 250 characters.";
        usedIds.add(message.getMessageId());
        if (choice == SendChoice.STORE) {
            storedMessages.add(message);
            persistStoredMessages();
            return "Message successfully stored.";
        }
        sentMessages.add(message);
        return "Message successfully sent.";
    }

    public void storeMessage(Message message) {
        if (message == null) throw new IllegalArgumentException("Message cannot be null.");
        if (!storedMessages.contains(message)) storedMessages.add(message);
        persistStoredMessages();
    }

    private void persistStoredMessages() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            if (i > 0) json.append(",\n");
            json.append(storedMessages.get(i).toJson());
        }
        json.append("\n]\n");
        try {
            Path parent = jsonFile.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(jsonFile, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not save stored messages to " + jsonFile + ": " + ex.getMessage(), ex);
        }
    }

    /** Reads stored message objects from the JSON file into the in-memory collection. */
    public final void loadStoredMessages() {
        if (!Files.exists(jsonFile)) return;
        try {
            String json = Files.readString(jsonFile, StandardCharsets.UTF_8);
            Matcher objects = JSON_OBJECT.matcher(json);
            while (objects.find()) {
                int storedNumber = nextMessageNumber;
                Matcher numberField = JSON_NUMBER.matcher(objects.group(1));
                if (numberField.find()) storedNumber = Integer.parseInt(numberField.group(1));
                Matcher fields = JSON_FIELD.matcher(objects.group(1));
                String id = "", sender = "You", recipient = "", body = "";
                while (fields.find()) {
                    String value = Message.unescapeJson(fields.group(2));
                    switch (fields.group(1)) {
                        case "messageId" -> id = value;
                        case "sender" -> sender = value;
                        case "recipient" -> recipient = value;
                        case "message" -> body = value;
                        default -> { }
                    }
                }
                if (!id.isBlank() && !containsId(storedMessages, id)) {
                    Message loaded = new Message(id, storedNumber, sender, recipient, body);
                    storedMessages.add(loaded);
                    usedIds.add(id);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read stored messages: " + ex.getMessage(), ex);
        }
    }

    private boolean containsId(List<Message> messages, String id) {
        return messages.stream().anyMatch(m -> m.getMessageId().equals(id));
    }

    public String printMessages() {
        if (sentMessages.isEmpty()) return "No messages have been sent.";
        return joinDetails(sentMessages);
    }

    public String getRecentMessages(int maximum) {
        if (maximum < 1) return "Enter a positive number of recent messages.";
        if (sentMessages.isEmpty()) return "No messages have been sent.";
        int start = Math.max(0, sentMessages.size() - maximum);
        return joinDetails(sentMessages.subList(start, sentMessages.size()));
    }

    public String displayStoredSenderRecipient() {
        if (storedMessages.isEmpty()) return "No stored messages.";
        StringBuilder out = new StringBuilder();
        for (Message m : storedMessages) {
            if (out.length() > 0) out.append("\n");
            out.append("Sender: ").append(m.getSender())
               .append("\nRecipient: ").append(m.getRecipient()).append("\n");
        }
        return out.toString();
    }

    public String getLongestStoredMessage() {
        return storedMessages.stream().max((a,b) -> Integer.compare(a.getText().length(), b.getText().length()))
            .map(Message::getText).orElse("No stored messages.");
    }

    public String searchMessageId(String id) {
        return findById(id).map(m -> "Recipient: " + m.getRecipient() + "\nMessage: " + m.getText())
            .orElse("No message found for ID: " + id);
    }

    public String searchStoredByRecipient(String recipient) {
        List<Message> matches = storedMessages.stream().filter(m -> m.getRecipient().equals(recipient)).toList();
        return matches.isEmpty() ? "No stored messages found for recipient: " + recipient
            : matches.stream().map(Message::getText).reduce((a,b) -> a + "\n" + b).orElse("");
    }

    public String deleteStoredByHash(String hash) {
        for (int i = 0; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).getHash().equalsIgnoreCase(hash)) {
                Message removed = storedMessages.remove(i);
                persistStoredMessages();
                return "Message: \"" + removed.getText() + "\" successfully deleted.";
            }
        }
        return "No stored message found with hash: " + hash;
    }

    public String displayStoredReport() {
        if (storedMessages.isEmpty()) return "No stored messages.";
        return joinDetails(storedMessages);
    }

    private java.util.Optional<Message> findById(String id) {
        for (Message m : allMessages()) if (m.getMessageId().equals(id)) return java.util.Optional.of(m);
        return java.util.Optional.empty();
    }

    private List<Message> allMessages() {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages); all.addAll(storedMessages); all.addAll(disregardedMessages);
        return all;
    }

    private String joinDetails(List<Message> messages) {
        StringBuilder out = new StringBuilder();
        for (Message m : messages) {
            if (out.length() > 0) out.append("\n\n");
            out.append(m.getDetails());
        }
        return out.toString();
    }

    public int returnTotalMessages() { return sentMessages.size(); }
    public List<Message> getSentMessages() { return Collections.unmodifiableList(sentMessages); }
    public List<Message> getDisregardedMessages() { return Collections.unmodifiableList(disregardedMessages); }
    public List<Message> getStoredMessages() { return Collections.unmodifiableList(storedMessages); }

    // Dynamically populated parallel arrays required by the task brief.
    public String[] getSentMessageArray() { return sentMessages.stream().map(Message::getText).toArray(String[]::new); }
    public String[] getDisregardedMessageArray() { return disregardedMessages.stream().map(Message::getText).toArray(String[]::new); }
    public String[] getStoredMessageArray() { return storedMessages.stream().map(Message::getText).toArray(String[]::new); }
    public String[] getMessageHashArray() { return allMessages().stream().map(Message::getHash).toArray(String[]::new); }
    public String[] getMessageIdArray() { return allMessages().stream().map(Message::getMessageId).toArray(String[]::new); }
}
