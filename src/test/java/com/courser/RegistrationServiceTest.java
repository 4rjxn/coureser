package com.courser;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.courser.exception.CourseFullException;
import com.courser.exception.CreditLimitExceededException;
import com.courser.exception.DuplicateRegistrationException;
import com.courser.exception.PrerequisiteNotMetException;
import com.courser.exception.RegistrationException;
import com.courser.model.Course;
import com.courser.model.Registration;
import com.courser.model.Student;
import com.courser.services.CourseServices;
import com.courser.services.RegistrationService;
import com.courser.services.StudentServices;
import com.courser.utils.Database;

public class RegistrationServiceTest {

    @BeforeClass
    public static void initDb() {
        Database.init();
    }

    @Before
    public void setUp() {
        // Clean up or prepare test data
        try {
            CourseServices.removeCourse("TEST101");
            CourseServices.removeCourse("TEST102");
            CourseServices.removeCourse("TEST103");
            CourseServices.removeCourse("TEST_FULL");
            StudentServices.removeStudent("STU_TEST1");
            StudentServices.removeStudent("STU_TEST2");
        } catch (Exception ignored) {}
    }

    @Test
    public void testSuccessfulCourseRegistration() throws RegistrationException {
        // Setup course & student
        Course course = new Course("TEST101", "Test Course 1", 3, "Prof. Test", List.of(), 10);
        CourseServices.addNewCourse(course);

        Student student = new Student("testuser1", "STU_TEST1", "Test Student 1", "test1@univ.edu", 15);
        StudentServices.registerNewStudent(student, null);

        // Perform registration
        Registration reg = RegistrationService.registerCourse("STU_TEST1", "TEST101");
        assertNotNull(reg);
        assertEquals("STU_TEST1", reg.getStudent().getStudentId());
        assertEquals("TEST101", reg.getCourse().getCourseCode());
        assertTrue(reg.isActive());

        // Verify registered courses query
        List<Course> registered = RegistrationService.getRegisteredCoursesForStudent("STU_TEST1");
        assertEquals(1, registered.size());
        assertEquals("TEST101", registered.get(0).getCourseCode());
    }

    @Test(expected = DuplicateRegistrationException.class)
    public void testDuplicateRegistrationThrowsException() throws RegistrationException {
        Course course = new Course("TEST101", "Test Course 1", 3, "Prof. Test", List.of(), 10);
        CourseServices.addNewCourse(course);

        Student student = new Student("testuser1", "STU_TEST1", "Test Student 1", "test1@univ.edu", 15);
        StudentServices.registerNewStudent(student, null);

        // Register once
        RegistrationService.registerCourse("STU_TEST1", "TEST101");

        // Second registration should throw DuplicateRegistrationException
        RegistrationService.registerCourse("STU_TEST1", "TEST101");
    }

    @Test(expected = PrerequisiteNotMetException.class)
    public void testPrerequisiteRuleEnforcement() throws RegistrationException {
        // TEST102 requires TEST101
        Course course1 = new Course("TEST101", "Test Course 1", 3, "Prof. Test", List.of(), 10);
        Course course2 = new Course("TEST102", "Test Course 2", 3, "Prof. Test", List.of("TEST101"), 10);
        CourseServices.addNewCourse(course1);
        CourseServices.addNewCourse(course2);

        Student student = new Student("testuser1", "STU_TEST1", "Test Student 1", "test1@univ.edu", 15);
        StudentServices.registerNewStudent(student, null);

        // Attempt to register for TEST102 without completing TEST101
        RegistrationService.registerCourse("STU_TEST1", "TEST102");
    }

    @Test
    public void testRegistrationAllowedAfterPrerequisiteCompleted() throws RegistrationException {
        Course course1 = new Course("TEST101", "Test Course 1", 3, "Prof. Test", List.of(), 10);
        Course course2 = new Course("TEST102", "Test Course 2", 3, "Prof. Test", List.of("TEST101"), 10);
        CourseServices.addNewCourse(course1);
        CourseServices.addNewCourse(course2);

        Student student = new Student("testuser1", "STU_TEST1", "Test Student 1", "test1@univ.edu", 15);
        StudentServices.registerNewStudent(student, null);

        // Mark TEST101 as completed
        StudentServices.addCompletedCourse("STU_TEST1", "TEST101");

        // Now registration for TEST102 succeeds
        Registration reg = RegistrationService.registerCourse("STU_TEST1", "TEST102");
        assertNotNull(reg);
        assertEquals("TEST102", reg.getCourse().getCourseCode());
    }

    @Test(expected = CreditLimitExceededException.class)
    public void testCreditLimitRuleEnforcement() throws RegistrationException {
        // Student with max credits = 5
        Student student = new Student("testuser1", "STU_TEST1", "Test Student 1", "test1@univ.edu", 5);
        StudentServices.registerNewStudent(student, null);

        Course c1 = new Course("TEST101", "Test 1", 3, "Prof. Test", List.of(), 10);
        Course c2 = new Course("TEST102", "Test 2", 3, "Prof. Test", List.of(), 10);
        CourseServices.addNewCourse(c1);
        CourseServices.addNewCourse(c2);

        // Register for 3 credits (total = 3, allowed <= 5)
        RegistrationService.registerCourse("STU_TEST1", "TEST101");

        // Attempt to register for another 3 credits (3 + 3 = 6 > 5) -> CreditLimitExceededException
        RegistrationService.registerCourse("STU_TEST1", "TEST102");
    }

    @Test(expected = CourseFullException.class)
    public void testCourseCapacityLimitEnforcement() throws RegistrationException {
        // Course with capacity = 1
        Course smallCourse = new Course("TEST_FULL", "Small Seminar", 3, "Prof. Test", List.of(), 1);
        CourseServices.addNewCourse(smallCourse);

        Student s1 = new Student("testuser1", "STU_TEST1", "Student One", "s1@univ.edu", 20);
        Student s2 = new Student("testuser2", "STU_TEST2", "Student Two", "s2@univ.edu", 20);
        StudentServices.registerNewStudent(s1, null);
        StudentServices.registerNewStudent(s2, null);

        // First student gets the seat
        RegistrationService.registerCourse("STU_TEST1", "TEST_FULL");

        // Second student is rejected due to capacity limit
        RegistrationService.registerCourse("STU_TEST2", "TEST_FULL");
    }

    @Test
    public void testDropCourseAndFreeCapacity() throws RegistrationException {
        Course course = new Course("TEST101", "Test Course", 4, "Prof. Test", List.of(), 1);
        CourseServices.addNewCourse(course);

        Student s1 = new Student("testuser1", "STU_TEST1", "Student One", "s1@univ.edu", 20);
        Student s2 = new Student("testuser2", "STU_TEST2", "Student Two", "s2@univ.edu", 20);
        StudentServices.registerNewStudent(s1, null);
        StudentServices.registerNewStudent(s2, null);

        // S1 registers
        RegistrationService.registerCourse("STU_TEST1", "TEST101");

        // S1 drops course
        assertTrue(RegistrationService.dropCourse("STU_TEST1", "TEST101"));

        // Now S2 can register for the freed seat
        Registration reg = RegistrationService.registerCourse("STU_TEST2", "TEST101");
        assertNotNull(reg);
        assertEquals("STU_TEST2", reg.getStudent().getStudentId());
    }
}
