//package com.example.shiftmanagement.service;
//
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    private static final Map<String, String> userDb = new HashMap<>();
//
//    static {
//        // bcrypt encoded passwords for manager and employee
//        userDb.put("manager", new BCryptPasswordEncoder().encode("manager")); // password: manager
//        userDb.put("employee", new BCryptPasswordEncoder().encode("employee")); // password: employee
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        if (userDb.containsKey(username)) {
//            if (username.equals("manager")) {
//                return new User(username, userDb.get(username), Arrays.asList(new SimpleGrantedAuthority("ROLE_MANAGER")));
//            } else if (username.equals("employee")) {
//                return new User(username, userDb.get(username), Arrays.asList(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
//            }
//        }
//        throw new UsernameNotFoundException("User not found");
//    }
//}
