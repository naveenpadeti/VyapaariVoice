package com.vaanistock.demo;

import com.vaanistock.business.Business;
import com.vaanistock.business.BusinessRepository;
import com.vaanistock.business.BusinessType;
import com.vaanistock.category.Category;
import com.vaanistock.category.CategoryRepository;
import com.vaanistock.category.CategoryService;
import com.vaanistock.inventory.Inventory;
import com.vaanistock.inventory.InventoryRepository;
import com.vaanistock.product.Product;
import com.vaanistock.product.ProductRepository;
import com.vaanistock.transaction.Transaction;
import com.vaanistock.transaction.TransactionRepository;
import com.vaanistock.transaction.TransactionSource;
import com.vaanistock.transaction.TransactionType;
import com.vaanistock.user.User;
import com.vaanistock.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean demoEnabled;

    public DataInitializer(UserRepository userRepository,
                           BusinessRepository businessRepository,
                           CategoryRepository categoryRepository,
                           CategoryService categoryService,
                           ProductRepository productRepository,
                           InventoryRepository inventoryRepository,
                           TransactionRepository transactionRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${vaanistock.demo.enabled:true}") boolean demoEnabled) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoEnabled = demoEnabled;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoEnabled) return;
        if (userRepository.existsByEmail("demo@vaanistock.com")) return;

        // 1. Create Demo User
        User demoUser = new User(
                "Ravi Kumar",
                "9876543210",
                "demo@vaanistock.com",
                passwordEncoder.encode("demo123"),
                "te"
        );
        User savedUser = userRepository.save(demoUser);

        // 2. Create Demo Business
        Business demoBusiness = new Business(
                savedUser.getId(),
                "Sri Lakshmi Wholesale",
                BusinessType.WHOLESALE,
                "Vijayawada"
        );
        Business savedBusiness = businessRepository.save(demoBusiness);
        Long businessId = savedBusiness.getId();

        // 3. Seed Default Categories
        categoryService.seedDefaultCategories(businessId);
        Category grocery = categoryRepository.findByBusinessIdAndNameIgnoreCase(businessId, "Grocery")
                .orElseGet(() -> categoryRepository.save(new Category(businessId, "Grocery")));
        Category beverages = categoryRepository.findByBusinessIdAndNameIgnoreCase(businessId, "Beverages")
                .orElseGet(() -> categoryRepository.save(new Category(businessId, "Beverages")));
        Category foodItems = categoryRepository.findByBusinessIdAndNameIgnoreCase(businessId, "Food Items")
                .orElseGet(() -> categoryRepository.save(new Category(businessId, "Food Items")));
        Category stationery = categoryRepository.findByBusinessIdAndNameIgnoreCase(businessId, "Stationery")
                .orElseGet(() -> categoryRepository.save(new Category(businessId, "Stationery")));

        Instant now = Instant.now();

        // 4. Product: Rice (Matches Acceptance Test #77!)
        // Current stock: 25 bags, Reorder level: 15 bags, Target stock: 75 bags.
        // 30 days total sales: 150 bags -> Average sales = 5 bags/day! Coverage = 5 days! Refill = 50 bags!
        seedProductWithHistory(
                businessId, grocery.getId(),
                "Rice", "Premium Sona Masoori Rice (25kg Bag)", "bags",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("75"),
                new BigDecimal("25"),
                5, // 5 bags/day
                now
        );

        // 5. Product: Sugar (Low Stock)
        // Current stock: 8 kg, Reorder level: 20 kg, Target stock: 80 kg.
        // Average sales = 4 kg/day
        seedProductWithHistory(
                businessId, grocery.getId(),
                "Sugar", "Refined White Sugar", "kg",
                new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("80"),
                new BigDecimal("8"),
                4,
                now
        );

        // 6. Product: Cooking Oil (Low Stock + Fast Moving)
        // Current stock: 6 bottles, Reorder: 15, Target: 50
        // Average sales = 3 bottles/day
        seedProductWithHistory(
                businessId, grocery.getId(),
                "Cooking Oil", "Refined Sunflower Oil 1L", "bottles",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("50"),
                new BigDecimal("6"),
                3,
                now
        );

        // 7. Product: Biscuits (Slow Moving)
        // Current stock: 30 boxes, Reorder: 10, Target: 40
        // Sales: only 6 boxes in 30 days (~0.2/day)
        seedProductWithSpecificSales(
                businessId, foodItems.getId(),
                "Biscuits", "Assorted Butter Biscuits Box", "boxes",
                new BigDecimal("5"), new BigDecimal("10"), new BigDecimal("40"),
                new BigDecimal("30"),
                List.of(new SaleEvent(5, 2), new SaleEvent(15, 2), new SaleEvent(25, 2)),
                now
        );

        // 8. Product: Tea (Normal velocity)
        // Current stock: 35 packets, Reorder: 15, Target: 50
        seedProductWithHistory(
                businessId, beverages.getId(),
                "Tea", "Red Label Premium Tea 500g", "packets",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("50"),
                new BigDecimal("35"),
                1,
                now
        );

        // 9. Product: Coffee (Normal velocity)
        // Current stock: 28 packets, Reorder: 15, Target: 40
        seedProductWithHistory(
                businessId, beverages.getId(),
                "Coffee", "Bru Instant Coffee 200g", "packets",
                new BigDecimal("10"), new BigDecimal("15"), new BigDecimal("40"),
                new BigDecimal("28"),
                1,
                now
        );

        // 10. Product: Notebooks (No recent sales in 30 days!)
        // Current stock: 20 bundles
        seedProductWithSpecificSales(
                businessId, stationery.getId(),
                "Notebooks", "Long Ruled Notebook Bundles (12pcs)", "bundles",
                new BigDecimal("5"), new BigDecimal("10"), new BigDecimal("30"),
                new BigDecimal("20"),
                List.of(), // 0 sales in 30 days!
                now
        );

        // 11. Product: Pens (Slow Moving)
        // Current stock: 18 boxes
        seedProductWithSpecificSales(
                businessId, stationery.getId(),
                "Pens", "Blue Ballpoint Pens (Pack of 20)", "boxes",
                new BigDecimal("5"), new BigDecimal("10"), new BigDecimal("25"),
                new BigDecimal("18"),
                List.of(new SaleEvent(4, 3), new SaleEvent(18, 2), new SaleEvent(28, 3)),
                now
        );
    }

    private void seedProductWithHistory(Long businessId, Long categoryId,
                                        String name, String desc, String unit,
                                        BigDecimal minStock, BigDecimal reorder, BigDecimal target,
                                        BigDecimal currentStock, int dailyAvgSale, Instant now) {
        Product p = productRepository.save(new Product(businessId, categoryId, name, desc, unit, minStock, reorder, target));
        inventoryRepository.save(new Inventory(p.getId(), currentStock, unit));

        // Create initial stock addition
        BigDecimal totalSold = BigDecimal.valueOf(dailyAvgSale).multiply(BigDecimal.valueOf(30));
        BigDecimal initialStock = currentStock.add(totalSold);

        transactionRepository.save(new Transaction(
                businessId, p.getId(), TransactionType.ADD, initialStock, unit,
                TransactionSource.SYSTEM, "Initial warehouse stock", now.minus(Duration.ofDays(31))
        ));

        // Create distributed sales over the last 30 days
        for (int day = 1; day <= 30; day++) {
            transactionRepository.save(new Transaction(
                    businessId, p.getId(), TransactionType.SALE, BigDecimal.valueOf(dailyAvgSale), unit,
                    day % 4 == 0 ? TransactionSource.VOICE : TransactionSource.MANUAL,
                    "Customer sale - Day " + day,
                    now.minus(Duration.ofDays(30 - day + 1))
            ));
        }
    }

    private static class SaleEvent {
        int daysAgo;
        int qty;
        SaleEvent(int daysAgo, int qty) {
            this.daysAgo = daysAgo;
            this.qty = qty;
        }
    }

    private void seedProductWithSpecificSales(Long businessId, Long categoryId,
                                              String name, String desc, String unit,
                                              BigDecimal minStock, BigDecimal reorder, BigDecimal target,
                                              BigDecimal currentStock, List<SaleEvent> sales, Instant now) {
        Product p = productRepository.save(new Product(businessId, categoryId, name, desc, unit, minStock, reorder, target));
        inventoryRepository.save(new Inventory(p.getId(), currentStock, unit));

        BigDecimal totalSales = sales.stream()
                .map(s -> BigDecimal.valueOf(s.qty))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        transactionRepository.save(new Transaction(
                businessId, p.getId(), TransactionType.ADD, currentStock.add(totalSales), unit,
                TransactionSource.SYSTEM, "Initial warehouse stock", now.minus(Duration.ofDays(32))
        ));

        for (SaleEvent sale : sales) {
            transactionRepository.save(new Transaction(
                    businessId, p.getId(), TransactionType.SALE, BigDecimal.valueOf(sale.qty), unit,
                    TransactionSource.MANUAL, "Customer sale", now.minus(Duration.ofDays(sale.daysAgo))
            ));
        }
    }
}
