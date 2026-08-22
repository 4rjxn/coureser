package com.courser;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.courser.model.Course;
import com.courser.model.Student;

public class StudentTest {
    private Student student;
    private Course introCs;
    private Course dataStructures;
    private Course algorithms;

    @Before
    public void setUp() {
        student = new Student(1, "jdoe", "STU001", "John Doe", "jdoe@univ.edu", 15);

        introCs = new Course("CS101", "Intro to CS", 4, "Dr. Turing", List.of(), 30);
        dataStructures = new Course("CS102", "Data Structures", 4, "Dr. Turing", List.of("CS101"), 30);
        algorithms = new Course("CS201", "Algorithms", 4, "Dr. Knuth", List.of("CS101", "CS102"), 30);
    }

    @Test
    public void testStudentEncapsulationAndAttributes() {
        assertEquals("STU001", student.getStudentId());
        assertEquals("John Doe", student.getName());
        assertEquals("jdoe@univ.edu", student.getEmail());
        assertEquals(15, student.getMaxCredits());
        assertEquals(0, student.getRegisteredCredits());
        assertEquals(15, student.getRemainingCredits());

        student.setMaxCredits(18);
        assertEquals(18, student.getMaxCredits());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMaxCredits() {
        student.setMaxCredits(0);
    }

    @Test
    public void testRegisteredCreditsCalculation() {
        assertTrue(student.registerCourse(introCs));
        assertEquals(4, student.getRegisteredCredits());
        assertEquals(11, student.getRemainingCredits());

        Course math = new Course("MATH101", "Calculus I", 4, "Dr. Newton", List.of(), 30);
        assertTrue(student.registerCourse(math));
        assertEquals(8, student.getRegisteredCredits());
        assertEquals(7, student.getRemainingCredits());
    }

    @Test
    public void testPrerequisiteVerification() {
        // Initially no prerequisites completed
        assertFalse(student.hasCompletedPrerequisites(dataStructures));
        assertEquals(List.of("CS101"), student.getMissingPrerequisites(dataStructures));

        // Complete CS101
        student.addCompletedCourse("CS101");
        assertTrue(student.hasCompletedCourse("CS101"));
        assertTrue(student.hasCompletedPrerequisites(dataStructures));
        assertTrue(student.getMissingPrerequisites(dataStructures).isEmpty());

        // Multi-prerequisite check for Algorithms (requires CS101 and CS102)
        assertFalse(student.hasCompletedPrerequisites(algorithms));
        assertEquals(List.of("CS102"), student.getMissingPrerequisites(algorithms));

        student.addCompletedCourse("CS102");
        assertTrue(student.hasCompletedPrerequisites(algorithms));
    }

    @Test
    public void testCanRegisterRules() {
        // Eligible for CS101
        assertTrue(student.canRegister(introCs));

        // Ineligible for CS102 before prerequisite is completed
        assertFalse(student.canRegister(dataStructures));

        // After completing prerequisite
        student.addCompletedCourse("CS101");
        assertTrue(student.canRegister(dataStructures));

        // Register CS101, CS102, and Math (4+4+4 = 12 credits, max is 15)
        student.registerCourse(introCs);
        student.registerCourse(dataStructures);
        Course math = new Course("MATH101", "Calculus I", 4, "Dr. Newton", List.of(), 30);
        student.registerCourse(math);
        assertEquals(12, student.getRegisteredCredits());

        // Cannot register for 4 credit physics (12 + 4 = 16 > 15 max)
        Course physics = new Course("PHYS101", "Physics I", 4, "Dr. Einstein", List.of(), 30);
        assertFalse(student.canRegister(physics));

        // Duplicate registration check
        assertFalse(student.canRegister(introCs));
    }

    @Test
    public void testDropCourse() {
        student.registerCourse(introCs);
        assertTrue(student.isRegisteredFor("CS101"));
        assertEquals(4, student.getRegisteredCredits());

        assertTrue(student.dropCourse("CS101"));
        assertFalse(student.isRegisteredFor("CS101"));
        assertEquals(0, student.getRegisteredCredits());

        // Dropping non-registered course returns false
        assertFalse(student.dropCourse("CS101"));
    }
}
