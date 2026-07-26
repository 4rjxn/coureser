package com.courser;

import java.io.IOException;
import java.util.Scanner;

import com.courser.dao.UserDao;
import com.courser.model.Course;
import com.courser.model.Student;
import com.courser.services.CourseServices;
import com.courser.utils.Database;

public class App {
    public static void main(String args[]) throws IOException {
        Database.init();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;
        do {
            showMenu();
            String q = scanner.nextLine();
            switch (q) {
                case "a": {
                    handleAdd(scanner);
                    break;
                }
                case "b": {
                    Course course;
                    String chn_code;
                    String code;
                    String title;
                    int credits;
                    String name;
                    System.out.print("Enter course code To change > ");
                    chn_code = scanner.nextLine();
                    System.out.print("Enter course code > ");
                    code = scanner.nextLine();
                    System.out.print("Enter course title > ");
                    title = scanner.nextLine();
                    System.out.print("Enter credits > ");
                    credits = Integer.valueOf(scanner.nextLine());
                    System.out.print("Enter ins Name > ");
                    name = scanner.nextLine();
                    course = new Course(code, title, credits, name, null);
                    CourseServices.updateCourse(course, chn_code);
                    break;
                }
                case "c": {
                    handleDisplay();
                    break;
                }
                case "d": {
                    String username;
                    String studentId;
                    String name;
                    String email;
                    System.out.println("Enter username > ");
                    username = scanner.nextLine();
                    System.out.println("Enter id > ");
                    studentId = scanner.nextLine();
                    System.out.println("Enter name > ");
                    name = scanner.nextLine();
                    System.out.println("Enter email > ");
                    email = scanner.nextLine();
                    Student student = new Student(0, username, studentId, name, email, null);
                    try {

                        UserDao.addStudent(student);

                    } catch (Exception e) {
                        System.out.println(e);
                    }
                    break;
                }
                case "q": {
                    System.exit(0);
                }
            }

        } while (isRunning);
        scanner.close();

    }

    static void handleDisplay() {
        Course[] courses = CourseServices.getCourses(10, 0);
        for (Course c : courses) {
            System.out.println("\nCode: " + c.getCourseCode());
            System.out.println("Name: " + c.getTitle());
            System.out.println("Credits: " + c.getCredits());
            System.out.println("");
        }

    }

    static void handleAdd(Scanner scanner) {
        Course course;
        String code;
        String title;
        int credits;
        String name;
        System.out.print("Enter course code > ");
        code = scanner.nextLine();
        System.out.print("Enter course title > ");
        title = scanner.nextLine();
        System.out.print("Enter credits > ");
        credits = Integer.valueOf(scanner.nextLine());
        System.out.print("Enter ins Name > ");
        name = scanner.nextLine();
        course = new Course(code, title, credits, name, null);
        CourseServices.addNewCourse(course);
    }

    static void showMenu() {
        System.out.print("""
                Welcome
                a: Add course
                b: Update course
                c: Remove course
                c: List course
                d: add student
                q: Quit
                ::> """);
    }
}
