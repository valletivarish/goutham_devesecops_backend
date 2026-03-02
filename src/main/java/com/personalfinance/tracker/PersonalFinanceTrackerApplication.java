package com.personalfinance.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Personal Finance Budget Tracker application.
 * This Spring Boot application provides CRUD operations for managing
 * personal finances including transactions, budgets, categories, and
 * financial goals with ML-based spending forecasting.
 */
@SpringBootApplication
public class PersonalFinanceTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalFinanceTrackerApplication.class, args);
    }
}
