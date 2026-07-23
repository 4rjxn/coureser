package com.courser;

import java.io.IOException;

import com.courser.model.Student;
import com.courser.services.UserServices;
import com.courser.utils.Database;

public class App {
    public static void main(String args[]) throws IOException {
        Database.init();
        Student s = new Student(0, "tnahoeeu", 0, "stnhaoeus", "anotohu", 0, null);
        UserServices.registerNewUser(s);

    }
}
