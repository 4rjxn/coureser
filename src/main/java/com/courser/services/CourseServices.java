package com.courser.services;

import com.courser.dao.CourseDao;
import com.courser.model.Course;

public class CourseServices {
    public static void addNewCourse(Course course) {
        // validate the course
        // TODO: course validatin
        // then call
        CourseDao.addNew(course);

    }

    public static Course[] getCourses(int limit, int offset) {
        return CourseDao.getCourses(limit, offset);
    }

    public static void updateCourse(Course course, String course_code) {
        CourseDao.updateCourse(course, course_code);
    }
}
