/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.poepart3;

import java.util.Locale;
import java.util.regex.Pattern;

/** Represents one validated QuickChat message. */
public class Message {
    public static final int MAX_MESSAGE_LENGTH = 250;
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile("^\\+\\d{1,12}$");

    private final String messageId;
    private final int messageNumber;
    private final String recipient;
    private final String sender;
    private final String text;
    private final String hash;

    public Message(String messageId, int messageNumber, String recipient, String text) {
        this(messageId, messageNumber, "You", recipient, text);
    }

    public Message(String messageId, int messageNumber, String sender, String recipient, String text) {
        this.messageId = messageId == null ? "" : messageId.trim();
        this.messageNumber = messageNumber;
        this.sender = sender == null ? "You" : sender.trim();
        this.recipient = recipient == null ? "" : recipient.trim();
        this.text = text == null ? "" : text;
        this.hash = buildHash();
    }

    public boolean checkMessageID() { return MESSAGE_ID_PATTERN.matcher(messageId).matches(); }
    public String describeMessageId() { return "Message ID generated: " + messageId; }
    public boolean isRecipientValid() { return RECIPIENT_PATTERN.matcher(recipient).matches(); }

    public String checkRecipientCell() {
        return isRecipientValid() ? "Cell phone number successfully captured."
            : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public boolean isLengthValid() { return !text.isBlank() && text.length() <= MAX_MESSAGE_LENGTH; }

    public String checkMessageLength() {
        if (text.isBlank()) return "Message cannot be blank.";
        int excess = text.length() - MAX_MESSAGE_LENGTH;
        return excess > 0 ? "Message exceeds 250 characters by " + excess + "; please reduce the size."
                          : "Message ready to send.";
    }

    public String createMessageHash() { return hash; }

    private String buildHash() {
        String trimmed = text.trim();
        String prefix = messageId.substring(0, Math.min(2, messageId.length()));
        if (trimmed.isEmpty()) return (prefix + ":" + messageNumber + ":EMPTY").toUpperCase(Locale.ROOT);
        String[] words = trimmed.split("\\s+");
        String first = stripPunctuation(words[0]);
        String last = stripPunctuation(words[words.length - 1]);
        if (first.isEmpty()) first = "MESSAGE";
        if (last.isEmpty()) last = first;
        return (prefix + ":" + messageNumber + ":" + first + last).toUpperCase(Locale.ROOT);
    }

    private static String stripPunctuation(String word) {
        return word.replaceAll("[^\\p{L}\\p{N}]", "");
    }

    public String getDetails() {
        return "Message ID: " + messageId + "\nMessage Hash: " + hash
                + "\nRecipient: " + recipient + "\nMessage: " + text;
    }

    /** JSON representation with RFC 8259-compatible string escaping.
     * @return  */
    public String toJson() {
        return " {\n"
            + "    \"messageId\": \"" + escapeJson(messageId) + "\",\n"
            + "    \"messageHash\": \"" + escapeJson(hash) + "\",\n"
            + "    \"messageNumber\": " + messageNumber + ",\n"
            + "    \"sender\": \"" + escapeJson(sender) + "\",\n"
            + "    \"recipient\": \"" + escapeJson(recipient) + "\",\n"
            + "    \"message\": \"" + escapeJson(text) + "\"\n"
            + "  }";
    }

    static String escapeJson(String value) {
        StringBuilder out = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
                }
            }
        }
        return out.toString();
    }

    static String unescapeJson(String value) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c != '\\' || i + 1 >= value.length()) { out.append(c); continue; }
            char next = value.charAt(++i);
            switch (next) {
                case '"', '\\', '/' -> out.append(next);
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> {
                    if (i + 4 < value.length()) {
                        out.append((char)Integer.parseInt(value.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                }
                default -> out.append(next);
            }
        }
        return out.toString();
    }

    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public String getHash() { return hash; }
}
