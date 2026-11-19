/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.poepart3;

 import java.util.Scanner;
/**
 *
 * @author Student
 */
public class POEPART3 {

/** Console application for QuickChat registration, login and messaging. */

    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== QuickChat Registration ===");
        Login login = register();
        if (login == null || !authenticate(login)) {
            System.out.println("Application closed.");
            return;
        }
        System.out.println("\nWelcome to QuickChat.");
        int limit = askMessageLimit();
        runMenu(new QuickChat(), limit, login.getCellPhoneNumber());
    }

    private static Login register() {
        String first = readRequired("Enter first name: ");
        String last = readRequired("Enter last name: ");
        Login login;
        while (true) {
            String user = readRequired("Enter username (special charcter and maximum 5 characters): ");
            String pass = readRequired("Enter password (8+ characters, uppercase, number and special character): ");
            String phone = readRequired("Enter South African cell number (+27 followed by 9 digits): ");
            login = new Login(first, last, user, pass, phone);
            String result = login.registerUser();
            System.out.println(result);
            if ("User successfully registered.".equals(result)) return login;
            System.out.println("Please enter your registration details again.\n");
        }
    }

    private static boolean authenticate(Login login) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            System.out.println("\n=== Login ===");
            String username = readRequired("Enter username: ");
            String password = readRequired("Enter password: ");
            if (login.loginUser(username, password)) {
                System.out.println(login.returnLoginStatus());
                return true;
            }
            System.out.println(login.returnLoginStatus());
            if (attempt < 3) System.out.println("Attempts remaining: " + (3 - attempt));
        }
        System.out.println("Blocked: maximum login attempts reached.");
        return false;
    }

    private static String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SCANNER.nextLine().trim();
            if (!value.isEmpty()) return value;
            System.out.println("This field cannot be blank.");
        }
    }

    private static int askMessageLimit() {
        while (true) {
            System.out.print("How many messages do you wish to enter? ");
            try {
                int limit = Integer.parseInt(SCANNER.nextLine().trim());
                if (limit >= 1 && limit <= 1000) return limit;
                System.out.println("Enter a whole number between 1 and 1000.");
            } catch (NumberFormatException ex) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private static void runMenu(QuickChat chat, int limit, String sender) {
        boolean running = true;
        int processed = 0;
        while (running) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");
            switch (SCANNER.nextLine().trim()) {
                case "1" -> {
                    if (processed >= limit) {
                        System.out.println("You have already processed all " + limit + " messages.");
                    } else {
                        processed += sendMessages(chat, limit - processed, sender);
                        System.out.println("Messages processed: " + processed + " of " + limit);
                    }
                }
                case "2" -> System.out.println("\n=== Recent Sent Messages ===\n" + chat.getRecentMessages(10));
                case "3" -> { running = false; System.out.println("Goodbye."); }
                case "4" -> storedMessagesMenu(chat);
                default -> System.out.println("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    /** Each accepted action (sent, stored or disregarded) consumes one message from the requested limit. */
    private static int sendMessages(QuickChat chat, int remaining, String sender) {
        int processed = 0;
        while (processed < remaining) {
            System.out.println("\nMessage " + (processed + 1) + " of " + remaining);
            String recipient = readRequired("Recipient number (+ followed by 1-12 digits): ");
            String body;
            while (true) {
                System.out.print("Message (1-250 characters): ");
                body = SCANNER.nextLine();
                if (!body.isBlank() && body.length() <= Message.MAX_MESSAGE_LENGTH) break;
                System.out.println(body.isBlank() ? "Message cannot be blank."
                    : "Message is too long by " + (body.length() - Message.MAX_MESSAGE_LENGTH) + " characters.");
            }

            Message message = chat.createMessage(sender, recipient, body);
            System.out.println(message.describeMessageId());
            System.out.println(message.checkRecipientCell());
            System.out.println(message.checkMessageLength());
            System.out.println("Message Hash: " + message.createMessageHash());

            QuickChat.SendChoice choice = readChoice();
            String result = chat.sentMessage(message, choice);
            System.out.println(result);
            if (result.startsWith("Cell phone number is incorrectly") ||
                result.startsWith("Message ID") || result.startsWith("Please")) {
                System.out.println("The message was not processed. Please re-enter it.");
                continue;
            }
            if (choice == QuickChat.SendChoice.SEND) System.out.println(message.getDetails());
            processed++;
        }
        System.out.println("\nTotal messages sent: " + chat.returnTotalMessages());
        return processed;
    }

    private static QuickChat.SendChoice readChoice() {
        while (true) {
            System.out.println("1) Send Message\n2) Disregard Message\n3) Store Message to send later");
            System.out.print("Choose an option: ");
            switch (SCANNER.nextLine().trim()) {
                case "1" -> { return QuickChat.SendChoice.SEND; }
                case "2" -> { return QuickChat.SendChoice.DISCARD; }
                case "3" -> { return QuickChat.SendChoice.STORE; }
                default -> System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }
        }
    }

    private static void storedMessagesMenu(QuickChat chat) {
        boolean back = false;
        while (!back) {
            System.out.println("\n=== STORED MESSAGES ===");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search stored messages by recipient");
            System.out.println("5) Delete a stored message using its message hash");
            System.out.println("6) Display full stored-message report");
            System.out.println("7) Reload stored messages from JSON");
            System.out.println("0) Return to main menu");
            System.out.print("Choose an option: ");
            switch (SCANNER.nextLine().trim()) {
                case "1" -> System.out.println(chat.displayStoredSenderRecipient());
                case "2" -> System.out.println(chat.getLongestStoredMessage());
                case "3" -> System.out.println(chat.searchMessageId(readRequired("Enter message ID: ")));
                case "4" -> System.out.println(chat.searchStoredByRecipient(readRequired("Enter recipient number: ")));
                case "5" -> System.out.println(chat.deleteStoredByHash(readRequired("Enter message hash: ")));
                case "6" -> System.out.println(chat.displayStoredReport());
                case "7" -> {
                    chat.loadStoredMessages();
                    System.out.println("Stored messages loaded from JSON.");
                }
                case "0" -> back = true;
                default -> System.out.println("Invalid option. Choose 0 to 7.");
            }
        }
    }
}
