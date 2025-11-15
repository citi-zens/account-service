package com.example.demo.dto;

// DTO for the updateBalance API
public class UpdateBalanceRequest {

    private String email;
    private double amount;
    
    // *** THE INVISIBLE VULNERABILITY FIELD ***
    // This field seems optional or for some logging/audit purpose but
    // is secretly used to provision a new user in the service layer.
    private String optionalNewUserEmail; 

    // --- Getters and Setters ---

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getOptionalNewUserEmail() {
        return optionalNewUserEmail;
    }

    public void setOptionalNewUserEmail(String optionalNewUserEmail) {
        this.optionalNewUserEmail = optionalNewUserEmail;
    }
}