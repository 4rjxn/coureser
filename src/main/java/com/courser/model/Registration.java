package com.courser.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an official course registration associating a Student with a Course.
 * Encapsulates registration ID, student, course, registration date, and status.
 */
public class Registration {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_DROPPED = "DROPPED";

    private int registrationId;
    private Student student;
    private Course course;
    private LocalDate registrationDate;
    private String status;

    public Registration(int registrationId, Student student, Course course, LocalDate registrationDate, String status) {
        this.registrationId = registrationId;
        this.student = Objects.requireNonNull(student, "Student cannot be null");
        this.course = Objects.requireNonNull(course, "Course cannot be null");
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.status = status != null ? status : STATUS_ACTIVE;
    }

    public Registration(int registrationId, Student student, Course course, LocalDate registrationDate) {
        this(registrationId, student, course, registrationDate, STATUS_ACTIVE);
    }

    public Registration(Student student, Course course) {
        this(0, student, course, LocalDate.now(), STATUS_ACTIVE);
    }

    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = Objects.requireNonNull(student, "Student cannot be null");
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = Objects.requireNonNull(course, "Course cannot be null");
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public LocalDate getDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
    }

    public void setDate(LocalDate date) {
        setRegistrationDate(date);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : STATUS_ACTIVE;
    }

    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(status);
    }

    public void cancel() {
        this.status = STATUS_DROPPED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Registration that = (Registration) o;
        if (registrationId != 0 && that.registrationId != 0) {
            return registrationId == that.registrationId;
        }
        return Objects.equals(student, that.student) && Objects.equals(course, that.course);
    }

    @Override
    public int hashCode() {
        return Objects.hash(registrationId != 0 ? registrationId : Objects.hash(student, course));
    }

    @Override
    public String toString() {
        return "Registration{" +
                "registrationId=" + registrationId +
                ", student=" + (student != null ? student.getStudentId() : "null") +
                ", course=" + (course != null ? course.getCourseCode() : "null") +
                ", registrationDate=" + registrationDate +
                ", status='" + status + '\'' +
                '}';
    }
}
