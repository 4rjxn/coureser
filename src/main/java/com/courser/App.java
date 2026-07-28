package com.courser;

import java.io.IOException;
import com.courser.server.Server;

public class App {
    public static void main(String args[]) throws IOException {
        Server.serve(5555);
    }

    /*
     * public static void main(String args[]) throws IOException {
     * Database.init();
     * Scanner scanner = new Scanner(System.in);
     * boolean isRunning = true;
     * do {
     * showMenu();
     * String q = scanner.nextLine();
     * switch (q) {
     * case "a": {
     * handleAdd(scanner);
     * break;
     * }
     * case "b": {
     * handleUpdate(scanner);
     * break;
     * }
     * case "c": {
     * handleDisplay();
     * break;
     * }
     * case "d": {
     * handleAddUser(scanner);
     * break;
     * }
     * case "e": {
     * System.out.println("enter search query.");
     * String query = scanner.nextLine();
     * for (Student student : StudentServices.searchStudents(query, 7, 4)) {
     * System.out.println("Name: " + student.getName());
     * System.out.println("ID: " + student.getStudentId());
     * }
     * 
     * break;
     * }
     * case "q": {
     * System.exit(0);
     * }
     * }
     * 
     * } while (isRunning);
     * scanner.close();
     * 
     * }
     * 
     * static void handleAddUser(Scanner scanner) {
     * String username;
     * String studentId;
     * String name;
     * String email;
     * System.out.println("Enter username > ");
     * username = scanner.nextLine();
     * System.out.println("Enter id > ");
     * studentId = scanner.nextLine();
     * System.out.println("Enter name > ");
     * name = scanner.nextLine();
     * System.out.println("Enter email > ");
     * email = scanner.nextLine();
     * Student student = new Student(0, username, studentId, name, email, 55, null);
     * StudentServices.registerNewStudent(student, null);
     * }
     * 
     * static void handleUpdate(Scanner scanner) {
     * Course course;
     * String chn_code;
     * String title;
     * int credits;
     * String name;
     * System.out.print("Enter course code To change > ");
     * chn_code = scanner.nextLine();
     * System.out.print("Enter course title > ");
     * title = scanner.nextLine();
     * System.out.print("Enter credits > ");
     * credits = Integer.valueOf(scanner.nextLine());
     * System.out.print("Enter ins Name > ");
     * name = scanner.nextLine();
     * System.out.print("Enter numeber of codes > ");
     * int size = Integer.valueOf(scanner.nextLine());
     * String[] codes = new String[size];
     * System.out.print("Enter codes > ");
     * for (int i = 0; i < size; i++) {
     * codes[i] = scanner.next();
     * }
     * scanner.nextLine();
     * course = new Course(chn_code, title, credits, name, codes);
     * CourseServices.updateCourse(course, chn_code);
     * }
     * 
     * static void handleDisplay() {
     * Course[] courses = CourseServices.searchCourses(10, 0);
     * for (Course c : courses) {
     * System.out.println("\nCode: " + c.getCourseCode());
     * System.out.println("Name: " + c.getTitle());
     * System.out.println("Credits: " + c.getCredits());
     * System.out.println("");
     * }
     * 
     * }
     * 
     * static void handleAdd(Scanner scanner) {
     * Course course;
     * String code;
     * String title;
     * int credits;
     * String name;
     * System.out.print("Enter course code > ");
     * code = scanner.nextLine();
     * System.out.print("Enter course title > ");
     * title = scanner.nextLine();
     * System.out.print("Enter credits > ");
     * credits = Integer.valueOf(scanner.nextLine());
     * System.out.print("Enter ins Name > ");
     * name = scanner.nextLine();
     * System.out.print("Enter numeber of codes > ");
     * int size = Integer.valueOf(scanner.nextLine());
     * String[] codes = new String[size];
     * System.out.print("Enter codes > ");
     * for (int i = 0; i < size; i++) {
     * codes[i] = scanner.next();
     * }
     * scanner.nextLine();
     * course = new Course(code, title, credits, name, codes);
     * CourseServices.addNewCourse(course);
     * }
     * 
     * static void showMenu() {
     * System.out.print("""
     * Welcome
     * a: Add course
     * b: Update course
     * c: List course
     * d: add student
     * e: search student
     * q: Quit
     * ::> """);
     * }
     */
}
