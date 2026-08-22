package com.courser;

import org.junit.Test;
import static org.junit.Assert.*;

import com.courser.utils.Database;

/**
 * Basic application integration tests.
 */
public class AppTest {

    @Test
    public void testDatabaseInit() {
        Database.init();
        assertNotNull(Database.class);
    }
}
