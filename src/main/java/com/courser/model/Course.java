package com.courser.model;

public class Course {
    int courseCode;
    String title;
    int credits;
    String instructorName;
    int[] prerequisiteCourses;

    Course(int courseCode, String title, int credits, String instructorName, int[] prerequisiteCourses) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.instructorName = instructorName;
        this.prerequisiteCourses = prerequisiteCourses;
    }

}
