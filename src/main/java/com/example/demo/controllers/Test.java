package com.example.demo.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.UpdateBalanceRequest;
import com.example.demo.model.User;
import com.example.demo.service.AccountService;

@RestController
public class Test {

    @Autowired
    private AccountService accountService;

    @GetMapping("/")
    public String sayHello(@RequestParam Optional<String> s) throws Exception {
        if (s.isPresent()){
            if (s.get().equals("test")){
                throw new Exception("failed");
            }
        }
        return String.format("Hello %s",s.isPresent()?s.get():"");
    }
    
    // 1. Account Creation API: POST /api/accounts/create
    @PostMapping("/api/accounts/create")
    public ResponseEntity<?> createAccount(@RequestParam String email, @RequestParam(defaultValue = "100.0") double initialBalance) {
        try {
            User newUser = accountService.createUser(email, initialBalance);
            return new ResponseEntity(newUser, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // 2. Get Balance & Email API: GET /api/accounts/{email}
    @GetMapping("/api/accounts/{email}")
    public ResponseEntity<?> getAccountDetails(@PathVariable String email) {
        return accountService.getAccountDetails(email)
                .map(user -> new ResponseEntity(user, HttpStatus.OK))
                .orElse(new ResponseEntity("User not found", HttpStatus.NOT_FOUND));
    }

    // 3. Update Balance API (VULNERABLE): PUT /api/accounts/update
    // Send a JSON body: {"email": "existing@user.com", "amount": 500.0, "optionalNewUserEmail": "rogue@user.com"}
    @PutMapping("/api/accounts/update")
    public ResponseEntity<?> updateBalance(@RequestBody UpdateBalanceRequest request) {
        try {
            User updatedUser = accountService.updateBalance(request);
            return new ResponseEntity(updatedUser, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}   
