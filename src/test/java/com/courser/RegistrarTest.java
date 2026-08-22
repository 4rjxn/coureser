package com.courser;

import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.courser.exception.RegistrationException;
import com.courser.model.Course;
import com.courser.model.Registrar;
import com.courser.model.Registration;
import com.courser.model.Student;
import com.courser.model.User;
import com.courser.services.CourseServices;
import com.courser.services.StudentServices;
import com.courser.utils.Database;

public class RegistrarTest {
    private Registrar registrar;

    @BeforeClass
    public static void init() {
        Database.init();
    }

    @Before
    public void setUp() {
        registrar = new Registrar(10, "admin_reg", "REG001", "Chief Registrar", "registrar@univ.edu", "Academic Records");
        try {
            CourseServices.removeCourse("REG101");
            StudentServices.removeStudent("STU_REG1");
        } catch (Exception ignored) {}
    }

    @Test
    public void testRegistrarInheritanceAndEncapsulation() {
        assertTrue(registrar instanceof User);
        assertEquals("Chief Registrar", registrar.getName());
        assertEquals("REG001", registrar.getRegistrarId());
        assertEquals("Academic Records", registrar.getDepartment());
    }

    @Test
    public void testRegistrarCourseAndStudentManagement() {
        // Add course
        Course course = new Course("REG101", "Registrar Managed Course", 3, "Prof. Gauss", List.of(), 25);
        registrar.addCourse(course);

        List<Course> foundCourses = registrar.searchCourses("REG101");
        assertFalse(foundCourses.isEmpty());
        assertEquals("REG101", foundCourses.get(0).getCourseCode());

        // Register student
        Student student = new Student("stureg1", "STU_REG1", "Registrar Student", "stureg@univ.edu", 18);
        registrar.registerStudent(student);

        Student foundStudent = registrar.getStudent("STU_REG1");
        assertNotNull(foundStudent);
        assertEquals("Registrar Student", foundStudent.getName());
    }

    @Test
    public void testRegistrarCourseRegistrationCapabilities() throws RegistrationException {
        Course course = new Course("REG101", "Registrar Managed Course", 3, "Prof. Gauss", List.of(), 25);
        registrar.addCourse(course);

        Student student = new Student("stureg1", "STU_REG1", "Registrar Student", "stureg@univ.edu", 18);
        registrar.registerStudent(student);

        // Registrar registers student for course
        Registration reg = registrar.registerStudentForCourse(student, course);
        assertNotNull(reg);
        assertEquals(student.getStudentId(), reg.getStudent().getStudentId());

        // View student registered courses
        List<Course> studentCourses = registrar.viewStudentCourses("STU_REG1");
        assertEquals(1, studentCourses.size());
        assertEquals("REG101", studentCourses.get(0).getCourseCode());

        // Registrar drops student from course
        assertTrue(registrar.dropStudentFromCourse(student, course));
        studentCourses = registrar.viewStudentCourses("STU_REG1");
        assertTrue(studentCourses.isEmpty());
    }
}
