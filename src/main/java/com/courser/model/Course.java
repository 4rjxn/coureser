package com.courser.model;

public class Course {
    private String courseCode;
    private String title;
    private int credits;
    private String instructorName;
    private String[] prerequisiteCourses;

    public Course(String courseCode, String title, int credits, String instructorName,
            String[] prerequisiteCourses) {
        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.instructorName = instructorName;
        this.prerequisiteCourses = prerequisiteCourses;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public int getCredits() {
        return credits;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public String[] getPrerequisiteCourses() {
        return prerequisiteCourses;
    }

}
