package com.courser.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.courser.exception.RegistrationException;
import com.courser.model.Course;
import com.courser.model.Registration;
import com.courser.model.Student;
import com.courser.model.Teacher;
import com.courser.services.CourseServices;
import com.courser.services.RegistrationService;
import com.courser.services.StudentServices;
import com.courser.services.SummaryService;
import com.courser.services.TeacherServices;
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
                Set<String> completedCodes = StudentServices.getCompletedCourses(student.getStudentId());
                StringBuilder completedRows = new StringBuilder();
                if (completedCodes.isEmpty()) {
                    completedRows.append(
                            "<tr><td colspan=\"4\" style=\"text-align: center; color: #64748b;\">No completed course history recorded.</td></tr>");
                } else {
                    for (String code : completedCodes) {
                        Course c = CourseServices.getCourseByCode(code);
                        String title = c != null ? c.getTitle() : "N/A";
                        int credits = c != null ? c.getCredits() : 0;
                        completedRows
                                .append("""
                                        <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%d</td>
                                            <td>
                                                <a href="/admin/students/remove-completed-course?userId=%d&studentId=%s&courseCode=%s" onclick="return confirm('Remove %s from completed courses?')">Remove</a>
                                            </td>
                                        </tr>
                                        """
                                        .formatted(
                                                escapeHtml(code),
                                                escapeHtml(title),
                                                credits,
                                                student.getUserId(),
                                                urlEncode(student.getStudentId()),
                                                urlEncode(code),
                                                escapeHtml(code)));
                    }
                }

                List<Course> allCourses = CourseServices.getAllCourses();
                StringBuilder completedOpts = new StringBuilder();
                for (Course c : allCourses) {
                    if (!completedCodes.contains(c.getCourseCode())) {
                        completedOpts.append("<option value=\"%s\">%s - %s (%d cr)</option>\n".formatted(
                                escapeHtml(c.getCourseCode()),
                                escapeHtml(c.getCourseCode()),
                                escapeHtml(c.getTitle()),
                                c.getCredits()));
                    }
                }
                if (completedOpts.length() == 0) {
                    completedOpts
                            .append("<option value=\"\" disabled>All available courses already completed</option>\n");
                }

                Map<String, String> values = Map.of(
                        "userId", String.valueOf(student.getUserId()),
                        "username", escapeHtml(student.getUserName()),
                        "name", escapeHtml(student.getName()),
                        "email", escapeHtml(student.getEmail()),
                        "studentId", escapeHtml(student.getStudentId()),
                        "maxCredits", String.valueOf(student.getMaxCredits()),
                        "completedCourseRows", completedRows.toString(),
                        "completedCourseOptions", completedOpts.toString());
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

    static void handleAdminStudentsAddCompletedCourse(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            int userId = ServerUtils.getIntParam(form, "userId", 0);
            String studentId = ServerUtils.getStringParam(form, "studentId", "");
            String courseCode = ServerUtils.getStringParam(form, "courseCode", "");

            if (!studentId.isBlank() && !courseCode.isBlank()) {
                StudentServices.addCompletedCourse(studentId, courseCode);
            }
            Server.redirect(exchange, "/admin/students/edit?userId=" + userId + "&studentId=" + urlEncode(studentId));
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminStudentsRemoveCompletedCourse(HttpExchange exchange) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        int userId = ServerUtils.getIntParam(params, "userId", 0);
        String studentId = ServerUtils.getStringParam(params, "studentId", "");
        String courseCode = ServerUtils.getStringParam(params, "courseCode", "");

        if (!studentId.isBlank() && !courseCode.isBlank()) {
            StudentServices.removeCompletedCourse(studentId, courseCode);
        }
        Server.redirect(exchange, "/admin/students/edit?userId=" + userId + "&studentId=" + urlEncode(studentId));
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

            List<com.courser.model.Teacher> allTeachers = TeacherServices.getAllTeachers();
            StringBuilder teacherOpts = new StringBuilder();
            if (allTeachers.isEmpty()) {
                teacherOpts.append("<option value=\"Staff\">Staff</option>\n");
            } else {
                for (com.courser.model.Teacher t : allTeachers) {
                    teacherOpts.append("<option value=\"%s\">%s (%s)</option>\n"
                            .formatted(escapeHtml(t.getName()), escapeHtml(t.getName()), escapeHtml(t.getTeacherId())));
                }
            }

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
            String description = ServerUtils.getFirstStringParam(formMulti, "description", "");
            int credits = ServerUtils.getFirstIntParam(formMulti, "credits", 3);
            int capacity = ServerUtils.getFirstIntParam(formMulti, "maxEnrollment", 30);
            String teacherId = ServerUtils.getFirstStringParam(formMulti, "teacherId", "Staff");
            List<String> prereqs = formMulti.get("prerequisites");

            if (!code.isBlank() && !title.isBlank()) {
                Course course = new Course(code, title, description, credits, teacherId, prereqs, capacity, 0);
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

            List<com.courser.model.Teacher> allTeachers = TeacherServices.getAllTeachers();
            StringBuilder teacherOpts = new StringBuilder();
            if (allTeachers.isEmpty()) {
                boolean isSel = "Staff".equalsIgnoreCase(course.getInstructorName());
                teacherOpts.append("<option value=\"Staff\" %s>Staff</option>\n".formatted(isSel ? "selected" : ""));
            } else {
                for (com.courser.model.Teacher t : allTeachers) {
                    boolean isSel = t.getName().equalsIgnoreCase(course.getInstructorName())
                            || t.getTeacherId().equalsIgnoreCase(course.getInstructorName());
                    teacherOpts.append("<option value=\"%s\" %s>%s (%s)</option>\n"
                            .formatted(escapeHtml(t.getName()), isSel ? "selected" : "", escapeHtml(t.getName()),
                                    escapeHtml(t.getTeacherId())));
                }
            }

            Map<String, String> values = Map.of(
                    "courseId", course.getCourseCode(),
                    "code", escapeHtml(course.getCourseCode()),
                    "title", escapeHtml(course.getTitle()),
                    "description", escapeHtml(course.getDescription()),
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
            String description = ServerUtils.getFirstStringParam(formMulti, "description", "");
            int credits = ServerUtils.getFirstIntParam(formMulti, "credits", 3);
            int capacity = ServerUtils.getFirstIntParam(formMulti, "maxEnrollment", 30);
            String teacherId = ServerUtils.getFirstStringParam(formMulti, "teacherId", "Staff");
            List<String> prereqs = formMulti.get("prerequisites");

            Course course = new Course(code, title, description, credits, teacherId, prereqs, capacity, 0);
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
        StringBuilder completedCourseRows = new StringBuilder();
        if (!selectedStudentId.isBlank()) {
            Student selectedStudent = StudentServices.getStudentByStudentId(selectedStudentId);
            if (selectedStudent != null) {
                selectedStudentDisplay = "%s (%s) - Credits: %d / %d (Remaining: %d)".formatted(
                        selectedStudent.getName(),
                        selectedStudent.getStudentId(),
                        selectedStudent.getRegisteredCredits(),
                        selectedStudent.getMaxCredits(),
                        selectedStudent.getRemainingCredits());

                Set<String> completedCodes = StudentServices.getCompletedCourses(selectedStudent.getStudentId());
                if (completedCodes.isEmpty()) {
                    completedCourseRows.append(
                            "<tr><td colspan=\"4\" style=\"text-align: center; color: #64748b;\">No completed course history recorded.</td></tr>");
                } else {
                    for (String code : completedCodes) {
                        Course c = CourseServices.getCourseByCode(code);
                        String title = c != null ? c.getTitle() : "N/A";
                        int credits = c != null ? c.getCredits() : 0;
                        completedCourseRows
                                .append("""
                                        <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%d</td>
                                            <td>
                                                <a href="/admin/students/remove-completed-course?userId=%d&studentId=%s&courseCode=%s" onclick="return confirm('Remove %s from completed courses?')">Remove</a>
                                            </td>
                                        </tr>
                                        """
                                        .formatted(
                                                escapeHtml(code),
                                                escapeHtml(title),
                                                credits,
                                                selectedStudent.getUserId(),
                                                urlEncode(selectedStudent.getStudentId()),
                                                urlEncode(code),
                                                escapeHtml(code)));
                    }
                }

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
        if (completedCourseRows.length() == 0) {
            completedCourseRows.append(
                    "<tr><td colspan=\"4\" style=\"text-align: center; color: #64748b;\">Select a student above to view completed courses.</td></tr>");
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
        values.put("completedCourseRows", completedCourseRows.toString());
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

    static void handleAdminRegistrationsAddCompleted(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            String studentId = ServerUtils.getStringParam(form, "studentId", "");
            String courseCode = ServerUtils.getStringParam(form, "courseCode", "");

            if (!studentId.isBlank() && !courseCode.isBlank()) {
                StudentServices.addCompletedCourse(studentId, courseCode);
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
    // Teacher Management
    // ==========================================
    static void handleAdminTeachers(HttpExchange exchange, String path) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        Map<String, String> values = new HashMap<>();

        String query = ServerUtils.getStringParam(params, "query", "");
        int page = ServerUtils.getIntParam(params, "page", 1);
        if (page < 1)
            page = 1;
        final int limit = 10;
        int offset = (page - 1) * limit;

        com.courser.model.Teacher[] teachers = TeacherServices.searchTeachers(query, limit, offset);
        int teacherCount = TeacherServices.getTotalCount();
        int totalPages = (int) Math.ceil((double) Math.max(1, teacherCount) / limit);

        StringBuilder rows = new StringBuilder();
        if (teachers != null) {
            for (com.courser.model.Teacher t : teachers) {
                String dept = TeacherServices.getTeacherDepartment(t.getUserId());
                rows.append(
                        """
                                <tr>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>%s</td>
                                    <td>
                                        <a href="/admin/teachers/edit?userId=%d">Edit</a> |
                                        <a href="/admin/teachers/delete?userId=%d" onclick="return confirm('Are you sure you want to delete teacher %s?')">Delete</a>
                                    </td>
                                </tr>
                                """
                                .formatted(
                                        escapeHtml(t.getTeacherId()),
                                        escapeHtml(t.getName()),
                                        escapeHtml(t.getUserName()),
                                        escapeHtml(t.getEmail()),
                                        escapeHtml(dept),
                                        t.getUserId(),
                                        t.getUserId(),
                                        escapeHtml(t.getTeacherId())));
            }
        }

        values.put("teacherRows", rows.toString());
        values.put("previousPage", "/admin/teachers?query=" + urlEncode(query) + "&page=" + Math.max(1, page - 1));
        values.put("currentPage", String.valueOf(page));
        values.put("totalPages", String.valueOf(totalPages));
        values.put("nextPage", "/admin/teachers?query=" + urlEncode(query) + "&page=" + Math.min(totalPages, page + 1));
        values.put("teacherCount", String.valueOf(teacherCount));
        values.put("query", escapeHtml(query));

        Server.respond(exchange, path, values);
    }

    static void handleAdminTeachersAdd(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Server.respond(exchange, path, Map.of());
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            String username = ServerUtils.getStringParam(form, "username", "");
            String teacherId = ServerUtils.getStringParam(form, "teacherId", "");
            String name = ServerUtils.getStringParam(form, "name", "");
            String email = ServerUtils.getStringParam(form, "email", "");
            String department = ServerUtils.getStringParam(form, "department", "");

            if (!username.isBlank() && !teacherId.isBlank() && !name.isBlank()) {
                com.courser.model.Teacher teacher = new com.courser.model.Teacher(username, name, email, teacherId);
                try {
                    TeacherServices.registerNewTeacher(teacher, department);
                } catch (Exception e) {
                    System.err.println("Failed to add teacher: " + e.getMessage());
                }
            }
            Server.redirect(exchange, "/admin/teachers");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminTeachersEdit(HttpExchange exchange, String path) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> params = ServerUtils.getQueryParams(exchange);
            int userId = ServerUtils.getIntParam(params, "userId", -1);
            String teacherId = ServerUtils.getStringParam(params, "teacherId", "");

            com.courser.model.Teacher teacher = null;
            if (userId > 0) {
                teacher = TeacherServices.getTeacherFromUserId(userId);
            } else if (!teacherId.isBlank()) {
                teacher = TeacherServices.getTeacherByTeacherId(teacherId);
            }

            if (teacher != null) {
                String dept = TeacherServices.getTeacherDepartment(teacher.getUserId());
                Map<String, String> values = Map.of(
                        "userId", String.valueOf(teacher.getUserId()),
                        "username", escapeHtml(teacher.getUserName()),
                        "name", escapeHtml(teacher.getName()),
                        "email", escapeHtml(teacher.getEmail()),
                        "teacherId", escapeHtml(teacher.getTeacherId()),
                        "department", escapeHtml(dept));
                Server.respond(exchange, path, values);
            } else {
                Server.redirect(exchange, "/admin/teachers");
            }
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = ServerUtils.getFormData(exchange);
            int userId = ServerUtils.getIntParam(form, "userId", 0);
            String username = ServerUtils.getStringParam(form, "username", "");
            String teacherId = ServerUtils.getStringParam(form, "teacherId", "");
            String name = ServerUtils.getStringParam(form, "name", "");
            String email = ServerUtils.getStringParam(form, "email", "");
            String department = ServerUtils.getStringParam(form, "department", "");

            com.courser.model.Teacher teacher = new com.courser.model.Teacher(userId, username, name, email, teacherId);
            TeacherServices.editTeacherDetails(teacher, department);
            Server.redirect(exchange, "/admin/teachers");
            return;
        }
        exchange.sendResponseHeaders(405, -1);
    }

    static void handleAdminTeachersDelete(HttpExchange exchange) throws IOException {
        Map<String, String> params = ServerUtils.getQueryParams(exchange);
        int userId = ServerUtils.getIntParam(params, "userId", -1);
        String teacherId = ServerUtils.getStringParam(params, "teacherId", "");

        if (userId > 0) {
            TeacherServices.removeTeacherByUserId(userId);
        } else if (!teacherId.isBlank()) {
            TeacherServices.removeTeacher(teacherId);
        }
        Server.redirect(exchange, "/admin/teachers");
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
