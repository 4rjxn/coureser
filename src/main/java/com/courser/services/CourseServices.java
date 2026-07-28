package com.courser.services;

import com.courser.dao.CourseDao;
import com.courser.model.Course;

public class CourseServices {
    public static void addNewCourse(Course course) {
        // validate the course
        // TODO: course validatin
        // then call
        try {
            CourseDao.addNew(course);
        } catch (Exception e) {
            System.out.println(e);

        }

    }

    public static Course[] searchCourses(String query, int limit, int offset) {
        return CourseDao.searchCourse(query, limit, offset);
    }

    public static void updateCourse(Course course, String course_code) {
        try {
            CourseDao.updateCourse(course, course_code);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
