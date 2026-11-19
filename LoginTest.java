package com.mycompany.poepart3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginTest {
    private final Login login = new Login("Kyle", "Smith", "kyl_1", "Ch&&sec@ke99!", "+27838968976");

    @Test
    void validDetailsRegisterSuccessfully() {
        assertEquals("User successfully registered.", login.registerUser());
    }

    @Test
    void invalidUsernameIsRejected() {
        assertFalse(new Login("A", "B", "kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976").checkUserName());
    }

    @Test
    void weakPasswordIsRejected() {
        assertFalse(new Login("A", "B", "kyl_1", "password", "+27838968976").checkPasswordComplexity());
    }

    @Test
    void phoneWithoutInternationalCodeIsRejected() {
        assertFalse(new Login("A", "B", "kyl_1", "Ch&&sec@ke99!", "08966553").checkCellPhoneNumber());
    }

    @Test
    void correctCredentialsLogIn() {
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
        assertEquals("Welcome Kyle, Smith it is great to see you again.", login.returnLoginStatus());
    }

    @Test
    void wrongCredentialsFailToLogIn() {
        assertFalse(login.loginUser("kyl_1", "wrong"));
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus());
    }
}
