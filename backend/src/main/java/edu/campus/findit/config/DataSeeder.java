package edu.campus.findit.config;

import edu.campus.findit.model.Category;
import edu.campus.findit.model.User;
import edu.campus.findit.model.enums.Role;
import edu.campus.findit.repository.CategoryRepository;
import edu.campus.findit.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedCategories();
        seedDefaultUsers();
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> defaultCategories = List.of(
                    new Category("Electronics & Gadgets", "Laptops, chargers, headphones, calculators, USB drives and cables", "laptop"),
                    new Category("Bags & Wallets", "Backpacks, side bags, clutches, money wallets and card holders", "briefcase"),
                    new Category("Books & Stationery", "Course textbooks, notebooks, pencil cases, geometry boxes and folders", "book"),
                    new Category("Keys & Access Cards", "Hostel dorm room keys, vehicle keys, lab smart cards and lanyard badges", "key"),
                    new Category("Clothing & Accessories", "Jackets, hoodies, caps, sunglasses, wristwatches and umbrellas", "shirt"),
                    new Category("ID Cards & Documents", "University ID cards, driver licenses, exam admission tickets and certificates", "file-text"),
                    new Category("Water Bottles & Flasks", "Insulated thermos flasks, metal gym bottles and tumblers", "cup-soda"),
                    new Category("Other Personal Items", "Sports equipment, musical instruments, jewelry and miscellaneous belongings", "box")
            );
            categoryRepository.saveAll(defaultCategories);
        }
    }

    private void seedDefaultUsers() {
        // Ensure Admin exists for Retrivo
        userRepository.findByEmail("admin@retrivo.campus.edu").ifPresentOrElse(
                admin -> {
                    admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
                    userRepository.save(admin);
                },
                () -> {
                    User admin = new User(
                            "Campus Security Administrator",
                            "admin@retrivo.campus.edu",
                            passwordEncoder.encode("Admin@123"),
                            Role.ADMIN,
                            "+1-555-0100"
                    );
                    userRepository.save(admin);
                }
        );

        // Also ensure legacy admin alias exists
        userRepository.findByEmail("admin@findit.campus.edu").ifPresentOrElse(
                admin -> {
                    admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
                    userRepository.save(admin);
                },
                () -> {
                    User admin = new User(
                            "Campus Security Administrator",
                            "admin@findit.campus.edu",
                            passwordEncoder.encode("Admin@123"),
                            Role.ADMIN,
                            "+1-555-0100"
                    );
                    userRepository.save(admin);
                }
        );

        // Ensure default student demo account exists with Student@123
        userRepository.findByEmail("suyash@campus.edu").ifPresentOrElse(
                student -> {
                    student.setPasswordHash(passwordEncoder.encode("Student@123"));
                    userRepository.save(student);
                },
                () -> {
                    User student = new User(
                            "Suyash Kaistha",
                            "suyash@campus.edu",
                            passwordEncoder.encode("Student@123"),
                            Role.STUDENT,
                            "+1-555-0101"
                    );
                    userRepository.save(student);
                }
        );

        // Ensure other demo students also have active Student@123 passwords
        String[] studentEmails = {"aarav@campus.edu", "ananya@campus.edu", "rohan@campus.edu", "priya@campus.edu"};
        for (String email : studentEmails) {
            userRepository.findByEmail(email).ifPresent(u -> {
                u.setPasswordHash(passwordEncoder.encode("Student@123"));
                userRepository.save(u);
            });
        }
    }
}

