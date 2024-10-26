package com.example.shiftmanagement.service;  

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.example.shiftmanagement.model.ImmutableUserDetails;

@Component
public class DepartmentChecker {

    public boolean check(Authentication authentication, String requiredDepartment) {
        ImmutableUserDetails userDetails = (ImmutableUserDetails) authentication.getPrincipal();
        return userDetails.getDepartment().equals(requiredDepartment);
    }
}
