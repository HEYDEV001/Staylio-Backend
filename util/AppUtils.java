package com.Backend.Projects.AirBnb.util;


import com.Backend.Projects.AirBnb.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {
    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
