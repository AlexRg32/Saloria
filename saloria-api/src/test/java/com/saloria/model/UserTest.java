package com.saloria.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  void isEnabledReflectsActiveFlag() {
    User activeUser = User.builder().active(true).build();
    User inactiveUser = User.builder().active(false).build();

    assertTrue(activeUser.isEnabled());
    assertFalse(inactiveUser.isEnabled());
  }
}
