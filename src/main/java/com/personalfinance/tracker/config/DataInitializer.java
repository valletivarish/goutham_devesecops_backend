package com.personalfinance.tracker.config;

import com.personalfinance.tracker.model.*;
import com.personalfinance.tracker.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds the database with realistic demo data on startup.
 * Skips entirely if the demo user already exists.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           TransactionRepository transactionRepository,
                           BudgetRepository budgetRepository,
                           FinancialGoalRepository financialGoalRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.financialGoalRepository = financialGoalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByUsername("demo")) {
            log.info("Demo data already exists — skipping seed.");
            return;
        }

        log.info("Seeding database with demo data...");

        // ── User ──────────────────────────────────────────────
        User user = userRepository.save(User.builder()
                .username("demo")
                .email("demo@personalfinance.com")
                .password(passwordEncoder.encode("demo1234"))
                .build());

        // ── Income Categories ─────────────────────────────────
        Category salary = saveCategory("Salary", TransactionType.INCOME, "💼", user);
        Category freelance = saveCategory("Freelance", TransactionType.INCOME, "💻", user);
        Category investments = saveCategory("Investments", TransactionType.INCOME, "📈", user);

        // ── Expense Categories ────────────────────────────────
        Category rent = saveCategory("Rent", TransactionType.EXPENSE, "🏠", user);
        Category groceries = saveCategory("Groceries", TransactionType.EXPENSE, "🛒", user);
        Category utilities = saveCategory("Utilities", TransactionType.EXPENSE, "💡", user);
        Category transport = saveCategory("Transportation", TransactionType.EXPENSE, "🚗", user);
        Category dining = saveCategory("Dining Out", TransactionType.EXPENSE, "🍽️", user);
        Category entertainment = saveCategory("Entertainment", TransactionType.EXPENSE, "🎬", user);
        Category healthcare = saveCategory("Healthcare", TransactionType.EXPENSE, "🏥", user);
        Category shopping = saveCategory("Shopping", TransactionType.EXPENSE, "🛍️", user);
        Category subscriptions = saveCategory("Subscriptions", TransactionType.EXPENSE, "📱", user);

        // ── Transactions (last 3 months) ──────────────────────
        LocalDate today = LocalDate.now();

        // --- Current month ---
        saveTxn(new BigDecimal("5200.00"), TransactionType.INCOME, "Monthly salary - March", today.withDayOfMonth(1), user, salary);
        saveTxn(new BigDecimal("800.00"), TransactionType.INCOME, "Website redesign project", today.withDayOfMonth(5), user, freelance);
        saveTxn(new BigDecimal("1500.00"), TransactionType.EXPENSE, "Monthly rent payment", today.withDayOfMonth(1), user, rent);
        saveTxn(new BigDecimal("85.50"), TransactionType.EXPENSE, "Weekly grocery run", today.withDayOfMonth(2), user, groceries);
        saveTxn(new BigDecimal("120.00"), TransactionType.EXPENSE, "Electricity bill", today.withDayOfMonth(3), user, utilities);
        saveTxn(new BigDecimal("45.00"), TransactionType.EXPENSE, "Gas station fill-up", today.withDayOfMonth(4), user, transport);
        saveTxn(new BigDecimal("62.30"), TransactionType.EXPENSE, "Dinner with friends", today.withDayOfMonth(6), user, dining);
        saveTxn(new BigDecimal("15.99"), TransactionType.EXPENSE, "Netflix subscription", today.withDayOfMonth(1), user, subscriptions);
        saveTxn(new BigDecimal("12.99"), TransactionType.EXPENSE, "Spotify Premium", today.withDayOfMonth(1), user, subscriptions);

        // --- Previous month ---
        LocalDate lastMonth = today.minusMonths(1);
        saveTxn(new BigDecimal("5200.00"), TransactionType.INCOME, "Monthly salary - February", lastMonth.withDayOfMonth(1), user, salary);
        saveTxn(new BigDecimal("450.00"), TransactionType.INCOME, "Freelance logo design", lastMonth.withDayOfMonth(12), user, freelance);
        saveTxn(new BigDecimal("150.00"), TransactionType.INCOME, "Dividend payout", lastMonth.withDayOfMonth(15), user, investments);
        saveTxn(new BigDecimal("1500.00"), TransactionType.EXPENSE, "Monthly rent payment", lastMonth.withDayOfMonth(1), user, rent);
        saveTxn(new BigDecimal("92.40"), TransactionType.EXPENSE, "Grocery shopping", lastMonth.withDayOfMonth(3), user, groceries);
        saveTxn(new BigDecimal("78.60"), TransactionType.EXPENSE, "Grocery run", lastMonth.withDayOfMonth(10), user, groceries);
        saveTxn(new BigDecimal("65.25"), TransactionType.EXPENSE, "Grocery essentials", lastMonth.withDayOfMonth(18), user, groceries);
        saveTxn(new BigDecimal("95.00"), TransactionType.EXPENSE, "Internet + phone bill", lastMonth.withDayOfMonth(5), user, utilities);
        saveTxn(new BigDecimal("110.00"), TransactionType.EXPENSE, "Electricity bill", lastMonth.withDayOfMonth(5), user, utilities);
        saveTxn(new BigDecimal("55.00"), TransactionType.EXPENSE, "Uber rides", lastMonth.withDayOfMonth(8), user, transport);
        saveTxn(new BigDecimal("42.00"), TransactionType.EXPENSE, "Gas station", lastMonth.withDayOfMonth(20), user, transport);
        saveTxn(new BigDecimal("38.50"), TransactionType.EXPENSE, "Lunch at Thai restaurant", lastMonth.withDayOfMonth(14), user, dining);
        saveTxn(new BigDecimal("75.00"), TransactionType.EXPENSE, "Birthday dinner out", lastMonth.withDayOfMonth(22), user, dining);
        saveTxn(new BigDecimal("35.00"), TransactionType.EXPENSE, "Movie tickets + popcorn", lastMonth.withDayOfMonth(16), user, entertainment);
        saveTxn(new BigDecimal("50.00"), TransactionType.EXPENSE, "Doctor co-pay", lastMonth.withDayOfMonth(11), user, healthcare);
        saveTxn(new BigDecimal("129.99"), TransactionType.EXPENSE, "New running shoes", lastMonth.withDayOfMonth(25), user, shopping);
        saveTxn(new BigDecimal("15.99"), TransactionType.EXPENSE, "Netflix subscription", lastMonth.withDayOfMonth(1), user, subscriptions);
        saveTxn(new BigDecimal("12.99"), TransactionType.EXPENSE, "Spotify Premium", lastMonth.withDayOfMonth(1), user, subscriptions);

        // --- Two months ago ---
        LocalDate twoMonthsAgo = today.minusMonths(2);
        saveTxn(new BigDecimal("5200.00"), TransactionType.INCOME, "Monthly salary - January", twoMonthsAgo.withDayOfMonth(1), user, salary);
        saveTxn(new BigDecimal("1200.00"), TransactionType.INCOME, "Freelance mobile app UI", twoMonthsAgo.withDayOfMonth(20), user, freelance);
        saveTxn(new BigDecimal("200.00"), TransactionType.INCOME, "Stock dividend", twoMonthsAgo.withDayOfMonth(10), user, investments);
        saveTxn(new BigDecimal("1500.00"), TransactionType.EXPENSE, "Monthly rent payment", twoMonthsAgo.withDayOfMonth(1), user, rent);
        saveTxn(new BigDecimal("88.00"), TransactionType.EXPENSE, "Grocery shopping", twoMonthsAgo.withDayOfMonth(4), user, groceries);
        saveTxn(new BigDecimal("72.30"), TransactionType.EXPENSE, "Grocery run", twoMonthsAgo.withDayOfMonth(12), user, groceries);
        saveTxn(new BigDecimal("105.00"), TransactionType.EXPENSE, "Electricity bill", twoMonthsAgo.withDayOfMonth(5), user, utilities);
        saveTxn(new BigDecimal("95.00"), TransactionType.EXPENSE, "Internet + phone bill", twoMonthsAgo.withDayOfMonth(5), user, utilities);
        saveTxn(new BigDecimal("60.00"), TransactionType.EXPENSE, "Monthly transit pass", twoMonthsAgo.withDayOfMonth(1), user, transport);
        saveTxn(new BigDecimal("48.75"), TransactionType.EXPENSE, "Sushi restaurant", twoMonthsAgo.withDayOfMonth(9), user, dining);
        saveTxn(new BigDecimal("25.00"), TransactionType.EXPENSE, "Concert streaming pass", twoMonthsAgo.withDayOfMonth(15), user, entertainment);
        saveTxn(new BigDecimal("85.00"), TransactionType.EXPENSE, "Pharmacy + vitamins", twoMonthsAgo.withDayOfMonth(7), user, healthcare);
        saveTxn(new BigDecimal("249.99"), TransactionType.EXPENSE, "Winter jacket on sale", twoMonthsAgo.withDayOfMonth(18), user, shopping);
        saveTxn(new BigDecimal("15.99"), TransactionType.EXPENSE, "Netflix subscription", twoMonthsAgo.withDayOfMonth(1), user, subscriptions);
        saveTxn(new BigDecimal("12.99"), TransactionType.EXPENSE, "Spotify Premium", twoMonthsAgo.withDayOfMonth(1), user, subscriptions);

        // ── Budgets (current month) ──────────────────────────
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        saveBudget(new BigDecimal("400.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, groceries);
        saveBudget(new BigDecimal("200.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, dining);
        saveBudget(new BigDecimal("250.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, utilities);
        saveBudget(new BigDecimal("150.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, transport);
        saveBudget(new BigDecimal("100.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, entertainment);
        saveBudget(new BigDecimal("200.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, shopping);
        saveBudget(new BigDecimal("50.00"), BudgetPeriod.MONTHLY, monthStart, monthEnd, user, subscriptions);

        // ── Financial Goals ───────────────────────────────────
        financialGoalRepository.saveAll(List.of(
                FinancialGoal.builder()
                        .name("Emergency Fund")
                        .targetAmount(new BigDecimal("10000.00"))
                        .currentAmount(new BigDecimal("6500.00"))
                        .deadline(LocalDate.now().plusMonths(6))
                        .status(GoalStatus.ACTIVE)
                        .user(user)
                        .build(),
                FinancialGoal.builder()
                        .name("Summer Vacation to Europe")
                        .targetAmount(new BigDecimal("3500.00"))
                        .currentAmount(new BigDecimal("1200.00"))
                        .deadline(LocalDate.now().plusMonths(4))
                        .status(GoalStatus.ACTIVE)
                        .user(user)
                        .build(),
                FinancialGoal.builder()
                        .name("New Laptop")
                        .targetAmount(new BigDecimal("2000.00"))
                        .currentAmount(new BigDecimal("2000.00"))
                        .deadline(LocalDate.now().minusMonths(1))
                        .status(GoalStatus.COMPLETED)
                        .user(user)
                        .build(),
                FinancialGoal.builder()
                        .name("Down Payment on Car")
                        .targetAmount(new BigDecimal("8000.00"))
                        .currentAmount(new BigDecimal("950.00"))
                        .deadline(LocalDate.now().plusYears(1))
                        .status(GoalStatus.ACTIVE)
                        .user(user)
                        .build()
        ));

        log.info("Demo data seeded successfully. Login with username: demo / password: demo1234");

        // ── Additional demo users ───────────────────────────
        if (!userRepository.existsByUsername("john")) {
            User john = userRepository.save(User.builder()
                    .username("john")
                    .email("john@personalfinance.com")
                    .password(passwordEncoder.encode("john1234"))
                    .build());
            saveCategory("Salary", TransactionType.INCOME, "💼", john);
            saveCategory("Groceries", TransactionType.EXPENSE, "🛒", john);
            log.info("User 'john' seeded. Login: john / john1234");
        }

        if (!userRepository.existsByUsername("sarah")) {
            User sarah = userRepository.save(User.builder()
                    .username("sarah")
                    .email("sarah@personalfinance.com")
                    .password(passwordEncoder.encode("sarah1234"))
                    .build());
            saveCategory("Salary", TransactionType.INCOME, "💼", sarah);
            saveCategory("Rent", TransactionType.EXPENSE, "🏠", sarah);
            log.info("User 'sarah' seeded. Login: sarah / sarah1234");
        }
    }

    private Category saveCategory(String name, TransactionType type, String icon, User user) {
        return categoryRepository.save(Category.builder()
                .name(name)
                .type(type)
                .icon(icon)
                .user(user)
                .build());
    }

    private void saveTxn(BigDecimal amount, TransactionType type, String description,
                         LocalDate date, User user, Category category) {
        transactionRepository.save(Transaction.builder()
                .amount(amount)
                .type(type)
                .description(description)
                .transactionDate(date)
                .user(user)
                .category(category)
                .build());
    }

    private void saveBudget(BigDecimal limit, BudgetPeriod period, LocalDate start,
                            LocalDate end, User user, Category category) {
        budgetRepository.save(Budget.builder()
                .amountLimit(limit)
                .period(period)
                .startDate(start)
                .endDate(end)
                .user(user)
                .category(category)
                .build());
    }
}
