# PROG5121 Part 3 POE – QuickChat

## Project overview
QuickChat is a Java console application for user registration, login and message management.

## Main features
- User registration and login validation.
- Sending, disregarding or storing messages.
- Message ID and hash generation.
- Recent sent-message reporting.
- Searching and reporting stored messages.
- JSON persistence for stored messages.
- JUnit 5 automated tests.

## Project structure
- `Login.java` – registration and authentication validation.
- `Message.java` – message data, validation, hash creation and JSON conversion.
- `QuickChat.java` – message workflow, collections, reports, searches and JSON persistence.
- `POEPART3.java` – application entry point and console menus.
- `src/test/...` – JUnit 5 tests.
- `.github/workflows/TestJava.yml` – GitHub Actions workflow.

## Requirements
- JDK 17 or newer.
- Apache Maven 3.8 or newer.
- NetBeans with Maven project support.

## Open and run in NetBeans
1. Extract the ZIP file.
2. In NetBeans, select **File → Open Project**.
3. Select the extracted `POEPART3` folder (the folder containing `pom.xml`).
4. Set the project Java platform to JDK 17 or newer.
5. Right-click the project and select **Clean and Build**.
6. Right-click the project and select **Run**.

The configured main class is `com.mycompany.poepart3.POEPART3`.

## Run from a terminal
From the folder containing `pom.xml`:

```bash
mvn clean test
mvn exec:java
```

## JSON storage
The application reads and writes `storedMessages.json` relative to the working directory. Run the project from its project root so that this file is found in the expected location.
