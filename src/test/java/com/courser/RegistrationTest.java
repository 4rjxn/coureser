package com.courser;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.courser.model.Course;
import com.courser.model.Registration;
import com.courser.model.Student;

public class RegistrationTest {
    private Student student;
    private Course course;

    @Before
    public void setUp() {
        student = new Student(1, "alice", "STU002", "Alice Smith", "alice@univ.edu", 18);
        course = new Course("CS201", "Algorithms", 4, "Dr. Knuth", List.of(), 30);
    }

    @Test
    public void testRegistrationCreationAndAttributes() {
        LocalDate today = LocalDate.now();
        Registration reg = new Registration(101, student, course, today, Registration.STATUS_ACTIVE);

        assertEquals(101, reg.getRegistrationId());
        assertEquals(student, reg.getStudent());
        assertEquals(course, reg.getCourse());
        assertEquals(today, reg.getRegistrationDate());
        assertEquals(Registration.STATUS_ACTIVE, reg.getStatus());
        assertTrue(reg.isActive());

        reg.cancel();
        assertEquals(Registration.STATUS_DROPPED, reg.getStatus());
        assertFalse(reg.isActive());
    }

    @Test(expected = NullPointerException.class)
    public void testNullStudentThrowsException() {
        new Registration(1, null, course, LocalDate.now(), Registration.STATUS_ACTIVE);
    }

    @Test(expected = NullPointerException.class)
    public void testNullCourseThrowsException() {
        new Registration(1, student, null, LocalDate.now(), Registration.STATUS_ACTIVE);
    }
}
