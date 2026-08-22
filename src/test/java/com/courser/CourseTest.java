package com.courser;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.courser.model.Course;

public class CourseTest {
    private Course course;

    @Before
    public void setUp() {
        course = new Course("CS101", "Introduction to Programming", 3, "Dr. Turing", List.of("MATH101"), 2);
    }

    @Test
    public void testCourseEncapsulation() {
        assertEquals("CS101", course.getCourseCode());
        assertEquals("Introduction to Programming", course.getTitle());
        assertEquals(3, course.getCredits());
        assertEquals("Dr. Turing", course.getInstructorName());
        assertEquals(2, course.getCapacity());
        assertEquals(0, course.getEnrolledCount());
        assertEquals(2, course.getAvailableSeats());
        assertFalse(course.isFull());
        assertTrue(course.hasPrerequisite("MATH101"));
    }

    @Test
    public void testCapacityAndEnrollment() {
        assertEquals(2, course.getAvailableSeats());

        // First student enrolls
        assertTrue(course.enroll());
        assertEquals(1, course.getEnrolledCount());
        assertEquals(1, course.getAvailableSeats());
        assertFalse(course.isFull());

        // Second student enrolls (reaches capacity of 2)
        assertTrue(course.enroll());
        assertEquals(2, course.getEnrolledCount());
        assertEquals(0, course.getAvailableSeats());
        assertTrue(course.isFull());

        // Third student attempts to enroll (should fail)
        assertFalse(course.enroll());
        assertEquals(2, course.getEnrolledCount());

        // Drop one student
        assertTrue(course.drop());
        assertEquals(1, course.getEnrolledCount());
        assertEquals(1, course.getAvailableSeats());
        assertFalse(course.isFull());
    }

    @Test
    public void testPrerequisitesManagement() {
        course.addPrerequisite("PHYS101");
        assertTrue(course.hasPrerequisite("PHYS101"));
        assertEquals(2, course.getPrerequisitesList().size());

        assertTrue(course.removePrerequisite("PHYS101"));
        assertFalse(course.hasPrerequisite("PHYS101"));
        assertEquals(1, course.getPrerequisitesList().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCredits() {
        course.setCredits(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCapacity() {
        course.setCapacity(-5);
    }
}
