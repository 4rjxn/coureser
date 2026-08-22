package com.courser.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.courser.exception.RegistrationException;
import com.courser.model.Course;
import com.courser.model.Registration;
import com.courser.model.Student;
import com.courser.services.CourseServices;
import com.courser.services.RegistrationService;
import com.courser.services.StudentServices;
import com.courser.services.SummaryService;
import com.sun.net.httpserver.HttpExchange;

class ServerHandles {

    // ==========================================
    // Dashboard
    // ==========================================
    static void handleAdminDashboard(HttpExchange exchange, String path) throws IOException {
        Map<String, String> summary = SummaryService.getSummary();
        Server.respond(exchange, path, summary);
    }

    // ==========================================
    // Student Management
    // ==========================================
    static void handleAdminStudents(HttpExchange exchange, String path) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        Map<String, String> values = new HashMap<>();

        String query = ServerUtils.getStringParam(params, "query", "");
        int page = ServerUtils.getIntParam(params, "page", 1);
        if (page < 1)
            page = 1;
        final int limit = 10;
        int offset = (page - 1) * limit;

        Student[] students = StudentServices.searchStudents(query, limit, offset);
        int studentCount = StudentServices.getTotalCount();
        int totalPages = (int) Math.ceil((double) Math.max(1, studentCount) / limit);

        StringBuilder rows = new StringBuilder();
        if (students != null) {
            for (Student s : students) {
                rows.append(
                        """
                                <tr>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%d</td>
                                    <td>
                                        <a href="/admin/students/edit?userId=%d">Edit</a> |
                                        <a href="/admin/registrations?studentId=%s">Register</a> |
                                        <a href="/admin/students/delete?userId=%d" onclick="return confirm('Are you sure you want to delete student %s?')">Delete</a>
                                    </td>
                                </tr>
                                """
                                .formatted(
                                        escapeHtml(s.getStudentId()),
                                        escapeHtml(s.getName()),
                                        escapeHtml(s.getUserName()),
                                        escapeHtml(s.getEmail()),
                                        s.getMaxCredits(),
                                        s.getUserId(),
                                        escapeHtml(s.getStudentId()),
                                        s.getUserId(),
                                        escapeHtml(s.getStudentId())));
            }
        }

        values.put("studentRows", rows.toString());
        values.put("previousPage", "/admin/students?query=" + urlEncode(query) + "&page=" + Math.max(1, page - 1));
        values.put("currentPage", String.valueOf(page));
        values.put("totalPages", String.valueOf(totalPages));
        values.put("nextPage", "/admin/students?query=" + urlEncode(query) + "&page=" + Math.min(totalPages, page + 1));
        values.put("studentCount", String.valueOf(studentCount));
        values.put("query", escapeHtml(query));

        Server.respond(exchange, path, values);
    }

    static void handleAdminStudentsAdd(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Server.respond(exchange, path, Map.of());
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            String username = ServerUtils.getStringParam(form, "username", "");
            String studentId = ServerUtils.getStringParam(form, "studentId", "");
            String name = ServerUtils.getStringParam(form, "name", "");
            String email = ServerUtils.getStringParam(form, "email", "");
            int maxCredits = ServerUtils.getIntParam(form, "maxCredits", 20);

            if (!username.isBlank() && !studentId.isBlank() && !name.isBlank()) {
                Student student = new Student(0, username, studentId, name, email, maxCredits);
                try {
                    StudentServices.registerNewStudent(student, null);
                } catch (Exception e) {
                    System.err.println("Failed to add student: " + e.getMessage());
                }
            }
            Server.redirect(exchange, "/admin/students");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminStudentsEdit(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> params = ServerUtils.getQueryParams(exchange);
            int userId = ServerUtils.getIntParam(params, "userId", -1);
            String studentId = ServerUtils.getStringParam(params, "studentId", "");

            Student student = null;
            if (userId > 0) {
                student = StudentServices.getStudentFromUserId(userId);
            } else if (!studentId.isBlank()) {
                student = StudentServices.getStudentByStudentId(studentId);
            }

            if (student != null) {
                Map<String, String> values = Map.of(
                        "userId", String.valueOf(student.getUserId()),
                        "username", escapeHtml(student.getUserName()),
                        "name", escapeHtml(student.getName()),
                        "email", escapeHtml(student.getEmail()),
                        "studentId", escapeHtml(student.getStudentId()),
                        "maxCredits", String.valueOf(student.getMaxCredits()));
                Server.respond(exchange, path, values);
            } else {
                Server.redirect(exchange, "/admin/students");
            }
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            int userId = ServerUtils.getIntParam(form, "userId", 0);
            String username = ServerUtils.getStringParam(form, "username", "");
            String studentId = ServerUtils.getStringParam(form, "studentId", "");
            String name = ServerUtils.getStringParam(form, "name", "");
            String email = ServerUtils.getStringParam(form, "email", "");
            int maxCredits = ServerUtils.getIntParam(form, "maxCredits", 20);

            Student student = new Student(userId, username, studentId, name, email, maxCredits);
            StudentServices.editStudentDetails(student);
            Server.redirect(exchange, "/admin/students");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminStudentsDelete(HttpExchange exchange) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        int userId = ServerUtils.getIntParam(params, "userId", -1);
        String studentId = ServerUtils.getStringParam(params, "studentId", "");

        if (userId > 0) {
            StudentServices.removeStudentByUserId(userId);
        } else if (!studentId.isBlank()) {
            StudentServices.removeStudent(studentId);
        }
        Server.redirect(exchange, "/admin/students");
    }

    // ==========================================
    // Course Management
    // ==========================================
    static void handleAdminCourses(HttpExchange exchange, String path) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        Map<String, String> values = new HashMap<>();

        String query = ServerUtils.getStringParam(params, "query", "");
        int page = ServerUtils.getIntParam(params, "page", 1);
        if (page < 1)
            page = 1;
        final int limit = 10;
        int offset = (page - 1) * limit;

        Course[] courses = CourseServices.searchCourses(query, limit, offset);
        int courseCount = CourseServices.getTotalCount();
        int totalPages = (int) Math.ceil((double) Math.max(1, courseCount) / limit);

        StringBuilder rows = new StringBuilder();
        if (courses != null) {
            for (Course c : courses) {
                String[] prereqs = c.getPrerequisiteCourses();
                String prereqStr = (prereqs != null && prereqs.length > 0) ? String.join(", ", prereqs) : "None";
                rows.append(
                        """
                                <tr>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%d</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>
                                        <a href="/admin/courses/edit?courseCode=%s">Edit</a> |
                                        <a href="/admin/courses/delete?courseCode=%s" onclick="return confirm('Are you sure you want to delete course %s?')">Delete</a>
                                    </td>
                                </tr>
                                """
                                .formatted(
                                        escapeHtml(c.getCourseCode()),
                                        escapeHtml(c.getTitle()),
                                        c.getCredits(),
                                        escapeHtml(c.getInstructorName()),
                                        escapeHtml(prereqStr),
                                        urlEncode(c.getCourseCode()),
                                        urlEncode(c.getCourseCode()),
                                        escapeHtml(c.getCourseCode())));
            }
        }

        values.put("courseRows", rows.toString());
        values.put("previousPage", "/admin/courses?query=" + urlEncode(query) + "&page=" + Math.max(1, page - 1));
        values.put("currentPage", String.valueOf(page));
        values.put("totalPages", String.valueOf(totalPages));
        values.put("nextPage", "/admin/courses?query=" + urlEncode(query) + "&page=" + Math.min(totalPages, page + 1));
        values.put("courseCount", String.valueOf(courseCount));
        values.put("query", escapeHtml(query));

        Server.respond(exchange, path, values);
    }

    static void handleAdminCoursesAdd(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Course> allCourses = CourseServices.getAllCourses();
            StringBuilder prereqOpts = new StringBuilder();
            for (Course c : allCourses) {
                prereqOpts.append("<option value=\"%s\">%s - %s</option>\n"
                        .formatted(escapeHtml(c.getCourseCode()), escapeHtml(c.getCourseCode()),
                                escapeHtml(c.getTitle())));
            }

            StringBuilder teacherOpts = new StringBuilder();
            teacherOpts.append("<option value=\"Prof. Alan Turing\">Prof. Alan Turing</option>\n");
            teacherOpts.append("<option value=\"Prof. Ada Lovelace\">Prof. Ada Lovelace</option>\n");
            teacherOpts.append("<option value=\"Prof. Grace Hopper\">Prof. Grace Hopper</option>\n");
            teacherOpts.append("<option value=\"Prof. Donald Knuth\">Prof. Donald Knuth</option>\n");
            teacherOpts.append("<option value=\"Dr. Claude Shannon\">Dr. Claude Shannon</option>\n");

            Map<String, String> values = Map.of(
                    "prerequisiteOptions", prereqOpts.toString(),
                    "teacherOptions", teacherOpts.toString());
            Server.respond(exchange, path, values);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, List<String>> formMulti = ServerUtils.getFormDataMulti(exchange);

            String code = ServerUtils.getFirstStringParam(formMulti, "code", "");
            String title = ServerUtils.getFirstStringParam(formMulti, "title", "");
            int credits = ServerUtils.getFirstIntParam(formMulti, "credits", 3);
            int capacity = ServerUtils.getFirstIntParam(formMulti, "maxEnrollment", 30);
            String teacherId = ServerUtils.getFirstStringParam(formMulti, "teacherId", "Staff");
            List<String> prereqs = formMulti.get("prerequisites");

            if (!code.isBlank() && !title.isBlank()) {
                Course course = new Course(code, title, credits, teacherId, prereqs, capacity);
                CourseServices.addNewCourse(course);
            }
            Server.redirect(exchange, "/admin/courses");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminCoursesEdit(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> params = ServerUtils.getQueryParams(exchange);
            String courseCode = ServerUtils.getStringParam(params, "courseCode", "");
            Course course = CourseServices.getCourseByCode(courseCode);
            if (course == null) {
                Server.redirect(exchange, "/admin/courses");
                return;
            }

            List<Course> allCourses = CourseServices.getAllCourses();
            StringBuilder prereqOpts = new StringBuilder();
            List<String> existingPrereqs = course.getPrerequisitesList();
            for (Course c : allCourses) {
                if (!c.getCourseCode().equalsIgnoreCase(course.getCourseCode())) {
                    boolean isSelected = existingPrereqs.contains(c.getCourseCode());
                    prereqOpts.append("<option value=\"%s\" %s>%s - %s</option>\n".formatted(
                            escapeHtml(c.getCourseCode()),
                            isSelected ? "selected" : "",
                            escapeHtml(c.getCourseCode()),
                            escapeHtml(c.getTitle())));
                }
            }

            StringBuilder teacherOpts = new StringBuilder();
            String[] teachers = {
                    "Prof. Alan Turing", "Prof. Ada Lovelace", "Prof. Grace Hopper",
                    "Prof. Donald Knuth", "Dr. Claude Shannon"
            };
            for (String t : teachers) {
                boolean isSel = t.equalsIgnoreCase(course.getInstructorName());
                teacherOpts.append("<option value=\"%s\" %s>%s</option>\n"
                        .formatted(escapeHtml(t), isSel ? "selected" : "", escapeHtml(t)));
            }

            Map<String, String> values = Map.of(
                    "courseId", course.getCourseCode(),
                    "code", escapeHtml(course.getCourseCode()),
                    "title", escapeHtml(course.getTitle()),
                    "description", "",
                    "credits", String.valueOf(course.getCredits()),
                    "maxEnrollment", String.valueOf(course.getCapacity()),
                    "teacherOptions", teacherOpts.toString(),
                    "prerequisiteOptions", prereqOpts.toString());
            Server.respond(exchange, path, values);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, List<String>> formMulti = ServerUtils.getFormDataMulti(exchange);

            String courseId = ServerUtils.getFirstStringParam(formMulti, "courseId", "");
            String code = ServerUtils.getFirstStringParam(formMulti, "code", "");
            String title = ServerUtils.getFirstStringParam(formMulti, "title", "");
            int credits = ServerUtils.getFirstIntParam(formMulti, "credits", 3);
            int capacity = ServerUtils.getFirstIntParam(formMulti, "maxEnrollment", 30);
            String teacherId = ServerUtils.getFirstStringParam(formMulti, "teacherId", "Staff");
            List<String> prereqs = formMulti.get("prerequisites");

            Course course = new Course(code, title, credits, teacherId, prereqs, capacity);
            CourseServices.updateCourse(course, courseId);
            Server.redirect(exchange, "/admin/courses");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminCoursesDelete(HttpExchange exchange) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        String courseCode = ServerUtils.getStringParam(params, "courseCode", "");
        if (!courseCode.isBlank()) {
            CourseServices.removeCourse(courseCode);
        }
        Server.redirect(exchange, "/admin/courses");
    }

    // ==========================================
    // Course Registrations
    // ==========================================
    static void handleAdminRegistrations(HttpExchange exchange, String path) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        Map<String, String> values = new HashMap<>();

        String studentQuery = ServerUtils.getStringParam(params, "query", "");
        String regQuery = ServerUtils.getStringParam(params, "registrationQuery", "");
        String selectedStudentId = ServerUtils.getStringParam(params, "studentId", "");
        int page = ServerUtils.getIntParam(params, "page", 1);
        if (page < 1)
            page = 1;
        final int limit = 10;
        int offset = (page - 1) * limit;

        // 1. Populate student selection table
        Student[] studentList = StudentServices.searchStudents(studentQuery, 10, 0);
        StringBuilder studentRows = new StringBuilder();
        if (studentList != null) {
            for (Student s : studentList) {
                studentRows.append("""
                        <tr>
                            <td>%s</td>
                            <td>%s</td>
                            <td><a href="/admin/registrations?studentId=%s&query=%s">Select</a></td>
                        </tr>
                        """.formatted(
                        escapeHtml(s.getStudentId()),
                        escapeHtml(s.getName()),
                        urlEncode(s.getStudentId()),
                        urlEncode(studentQuery)));
            }
        }

        // 2. Populate selected student info and course options
        String selectedStudentDisplay = "None selected (search and select a student above)";
        StringBuilder courseOptions = new StringBuilder();
        if (!selectedStudentId.isBlank()) {
            Student selectedStudent = StudentServices.getStudentByStudentId(selectedStudentId);
            if (selectedStudent != null) {
                selectedStudentDisplay = "%s (%s) - Credits: %d / %d (Remaining: %d)".formatted(
                        selectedStudent.getName(),
                        selectedStudent.getStudentId(),
                        selectedStudent.getRegisteredCredits(),
                        selectedStudent.getMaxCredits(),
                        selectedStudent.getRemainingCredits());

                List<Course> allCourses = CourseServices.getAllCourses();
                for (Course c : allCourses) {
                    boolean isRegistered = selectedStudent.isRegisteredFor(c.getCourseCode());
                    boolean hasPrereqs = selectedStudent.hasCompletedPrerequisites(c);
                    boolean hasCredits = (selectedStudent.getRegisteredCredits() + c.getCredits() <= selectedStudent
                            .getMaxCredits());
                    boolean isFull = c.isFull();

                    String statusNote = "";
                    if (isRegistered) {
                        statusNote = " [Already Registered]";
                    } else if (isFull) {
                        statusNote = " [FULL: %d/%d]".formatted(c.getEnrolledCount(), c.getCapacity());
                    } else if (!hasPrereqs) {
                        statusNote = " [Missing Prereqs: %s]"
                                .formatted(String.join(", ", selectedStudent.getMissingPrerequisites(c)));
                    } else if (!hasCredits) {
                        statusNote = " [Exceeds Credit Limit: +%d cr]".formatted(c.getCredits());
                    } else {
                        statusNote = " [Eligible - Seats: %d/%d]".formatted(c.getAvailableSeats(), c.getCapacity());
                    }

                    courseOptions.append("<option value=\"%s\" %s>%s - %s (%d cr)%s</option>\n".formatted(
                            escapeHtml(c.getCourseCode()),
                            isRegistered ? "disabled" : "",
                            escapeHtml(c.getCourseCode()),
                            escapeHtml(c.getTitle()),
                            c.getCredits(),
                            escapeHtml(statusNote)));
                }
            }
        }

        // 3. Populate current active registrations table
        List<Registration> registrations = RegistrationService.searchRegistrations(regQuery, limit, offset);
        int totalRegCount = RegistrationService.getTotalRegistrationsCount(regQuery);
        int totalPages = (int) Math.ceil((double) Math.max(1, totalRegCount) / limit);

        StringBuilder regRows = new StringBuilder();
        if (registrations != null) {
            for (Registration r : registrations) {
                regRows.append(
                        """
                                <tr>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%d</td>
                                    <td>
                                        <a href="/admin/registrations/drop?studentId=%s&courseCode=%s" onclick="return confirm('Are you sure you want to drop course %s for student %s?')">Drop</a>
                                    </td>
                                </tr>
                                """
                                .formatted(
                                        escapeHtml(r.getStudent().getStudentId()),
                                        escapeHtml(r.getStudent().getName()),
                                        escapeHtml(r.getCourse().getCourseCode()),
                                        escapeHtml(r.getCourse().getTitle()),
                                        r.getCourse().getCredits(),
                                        urlEncode(r.getStudent().getStudentId()),
                                        urlEncode(r.getCourse().getCourseCode()),
                                        escapeHtml(r.getCourse().getCourseCode()),
                                        escapeHtml(r.getStudent().getStudentId())));
            }
        }

        values.put("query", escapeHtml(studentQuery));
        values.put("studentRows", studentRows.toString());
        values.put("selectedStudent", escapeHtml(selectedStudentDisplay));
        values.put("studentId", escapeHtml(selectedStudentId));
        values.put("courseOptions", courseOptions.toString());
        values.put("registrationQuery", escapeHtml(regQuery));
        values.put("registrationRows", regRows.toString());
        values.put("previousPage", "/admin/registrations?studentId=" + urlEncode(selectedStudentId)
                + "&registrationQuery=" + urlEncode(regQuery) + "&page=" + Math.max(1, page - 1));
        values.put("currentPage", String.valueOf(page));
        values.put("totalPages", String.valueOf(totalPages));
        values.put("nextPage", "/admin/registrations?studentId=" + urlEncode(selectedStudentId) + "&registrationQuery="
                + urlEncode(regQuery) + "&page=" + Math.min(totalPages, page + 1));
        values.put("registrationCount", String.valueOf(totalRegCount));

        Server.respond(exchange, path, values);
    }

    static void handleAdminRegistrationsAdd(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleAdminRegistrations(exchange, path);
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            String studentId = ServerUtils.getStringParam(form, "studentId", "");
            String courseId = ServerUtils.getStringParam(form, "courseId", "");
            if (courseId.isBlank()) {
                courseId = ServerUtils.getStringParam(form, "courseCode", "");
            }

            if (!studentId.isBlank() && !courseId.isBlank()) {
                try {
                    RegistrationService.registerCourse(studentId, courseId);
                } catch (RegistrationException e) {
                    System.err.println("Registration rejected: " + e.getMessage());
                }
            }
            Server.redirect(exchange, "/admin/registrations?studentId=" + urlEncode(studentId));
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminRegistrationsDrop(HttpExchange exchange) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        String studentId = ServerUtils.getStringParam(params, "studentId", "");
        String courseCode = ServerUtils.getStringParam(params, "courseCode", "");

        if (!studentId.isBlank() && !courseCode.isBlank()) {
            RegistrationService.dropCourse(studentId, courseCode);
        }
        Server.redirect(exchange, "/admin/registrations?studentId=" + urlEncode(studentId));
    }

    // ==========================================
    // Utilities
    // ==========================================
    private static String escapeHtml(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String urlEncode(String input) {
        if (input == null)
            return "";
        return java.net.URLEncoder.encode(input, java.nio.charset.StandardCharsets.UTF_8);
    }
}
