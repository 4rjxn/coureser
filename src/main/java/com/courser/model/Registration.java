package com.courser.model;

import java.time.LocalDate;

class Registration {
    int registrationId;
    Student student;
    Course course;
    LocalDate date;

    Registration(int registrationId, Student student, Course course, LocalDate date) {
        this.registrationId = registrationId;
        this.student = student;
        this.course = course;
        this.date = date;
    }
}
