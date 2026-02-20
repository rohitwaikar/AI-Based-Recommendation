package com.demo;

import com.recommend.data.DataLoader;
import com.recommend.engine.*;
import com.recommend.model.*;

import java.io.IOException;
import java.util.*;

/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║    Java AI-Based Product Recommendation System             ║
 * ║    Algorithms: User-CF | Item-CF | Popularity | Hybrid     ║
 * ╚══════════════════════════════════════════════════════════════╝
 *
 * This is the main entry point. It:
 *   1. Loads sample data from CSV files
 *   2. Initializes all recommendation engines
 *   3. Runs an interactive console menu for exploration
 */
public class RecommendationApp {

    // ── Data Paths (classpath resources or filesystem) ────────────────────────
    private static final String RATINGS_FILE  = "ratings.csv";
    private static final String PRODUCTS_FILE = "products.csv";

    // ── Engines ───────────────────────────────────────────────────────────────
    private static UserBasedCF      userCF;
    private static ItemBasedCF      itemCF;
    private static PopularityEngine popularityEngine;
    private static HybridRecommender hybridRecommender;

    // ── Shared Data Structures ────────────────────────────────────────────────
    private static Map<Integer, Product>              productCatalog;
    private static Map<Integer, Map<Integer, Double>> userRatings;
    private static Map<Integer, Map<Integer, Double>> productUsers;
    private static List<Rating>                       allRatings;

    public static void main(String[] args) throws IOException {
        printBanner();

        // ── Step 1: Load Data ─────────────────────────────────────────────────
        System.out.println("► Loading product catalog and user ratings...");
        allRatings     = DataLoader.loadRatings(RATINGS_FILE);
        productCatalog = DataLoader.loadProducts(PRODUCTS_FILE);
        userRatings    = DataLoader.buildUserRatingIndex(allRatings);
        productUsers   = DataLoader.buildProductUserIndex(allRatings);

        System.out.printf("  ✅ Loaded %d products | %d users | %d ratings%n%n",
                productCatalog.size(), userRatings.size(), allRatings.size());

        // ── Step 2: Initialize Engines ────────────────────────────────────────
        System.out.println("► Initializing recommendation engines...");
        userCF           = new UserBasedCF(userRatings, productCatalog, 5);
        itemCF           = new ItemBasedCF(userRatings, productUsers, productCatalog);
        popularityEngine = new PopularityEngine(productUsers, productCatalog);
        hybridRecommender= new HybridRecommender(userCF, itemCF, popularityEngine);
        System.out.println("  ✅ All engines ready.\n");

        // ── Step 3: Run Demo + Interactive Menu ───────────────────────────────
        runDemo();
        runInteractiveMenu();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  AUTOMATED DEMO — shows all 4 algorithms for User 1
    // ─────────────────────────────────────────────────────────────────────────
    private static void runDemo() {
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  DEMO: Recommendations for User 1");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        displayUserProfile(1);

        System.out.println("\n┌─── [1] USER-BASED COLLABORATIVE FILTERING ────────");
        printRecommendations(userCF.recommend(1, 5, "pearson"));

        System.out.println("\n┌─── [2] ITEM-BASED COLLABORATIVE FILTERING ────────");
        printRecommendations(itemCF.recommend(1, 5));

        System.out.println("\n┌─── [3] POPULARITY-BASED (Trending) ───────────────");
        printRecommendations(popularityEngine.recommend(1, userRatings.get(1), 5));

        System.out.println("\n┌─── [4] HYBRID (50% UserCF + 35% ItemCF + 15% Pop) ");
        printRecommendations(hybridRecommender.recommend(1, userRatings.get(1), 5, "pearson"));

        System.out.println("\n┌─── [5] SIMILAR USERS to User 1 (Pearson) ─────────");
        List<Map.Entry<Integer, Double>> similarUsers = userCF.findSimilarUsers(1, 5, "pearson");
        for (Map.Entry<Integer, Double> entry : similarUsers) {
            System.out.printf("│  User %-3d → Similarity: %+.4f%n", entry.getKey(), entry.getValue());
        }

        System.out.println("\n┌─── [6] SIMILAR ITEMS to Product 101 ──────────────");
        List<Map.Entry<Integer, Double>> similarItems = itemCF.getMostSimilarItems(101, 5);
        for (Map.Entry<Integer, Double> entry : similarItems) {
            Product p = productCatalog.get(entry.getKey());
            System.out.printf("│  %-36s → Sim: %+.4f%n",
                    p != null ? p.getName() : "Unknown", entry.getValue());
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  INTERACTIVE MENU
    // ─────────────────────────────────────────────────────────────────────────
    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": menuUserBasedCF(scanner);  break;
                case "2": menuItemBasedCF(scanner);  break;
                case "3": menuPopularity(scanner);   break;
                case "4": menuHybrid(scanner);       break;
                case "5": menuProductDetails();      break;
                case "6": menuUserProfile(scanner);  break;
                case "7": menuDataStats();           break;
                case "0":
                    System.out.println("\n  👋 Goodbye!\n");
                    return;
                default:
                    System.out.println("  ⚠ Invalid choice.\n");
            }
        }
    }

    private static void menuUserBasedCF(Scanner sc) {
        System.out.print("  Enter User ID (1-10): ");
        int userId = readInt(sc, 1, 10);
        System.out.print("  Similarity metric [pearson/cosine/jaccard] (default=pearson): ");
        String metric = sc.nextLine().trim();
        if (metric.isEmpty()) metric = "pearson";
        System.out.print("  Number of recommendations (default=5): ");
        String nStr = sc.nextLine().trim();
        int n = nStr.isEmpty() ? 5 : Integer.parseInt(nStr);

        displayUserProfile(userId);
        System.out.println("\n  ── User-Based CF Recommendations ──────────────────");
        printRecommendations(userCF.recommend(userId, n, metric));

        System.out.println("\n  ── Most Similar Users ──────────────────────────────");
        List<Map.Entry<Integer, Double>> sims = userCF.findSimilarUsers(userId, 5, metric);
        for (Map.Entry<Integer, Double> e : sims) {
            System.out.printf("  User %-3d → Similarity: %+.4f%n", e.getKey(), e.getValue());
        }
        System.out.println();
    }

    private static void menuItemBasedCF(Scanner sc) {
        System.out.print("  Enter User ID (1-10): ");
        int userId = readInt(sc, 1, 10);
        displayUserProfile(userId);
        System.out.println("\n  ── Item-Based CF Recommendations ───────────────────");
        printRecommendations(itemCF.recommend(userId, 5));
        System.out.println();
    }

    private static void menuPopularity(Scanner sc) {
        System.out.println("  Select: [1] Global Trending  [2] By Category");
        String sub = sc.nextLine().trim();
        if ("2".equals(sub)) {
            System.out.print("  Category [Electronics/Books/Sports]: ");
            String cat = sc.nextLine().trim();
            System.out.println("\n  ── Top in " + cat + " ─────────────────────────────────────");
            printRecommendations(popularityEngine.recommendByCategory(cat, Collections.emptySet(), 5));
        } else {
            System.out.print("  Enter User ID for personalized filter (0=global): ");
            int uid = readInt(sc, 0, 10);
            Map<Integer, Double> rated = uid > 0 ? userRatings.getOrDefault(uid, new HashMap<>()) : new HashMap<>();
            System.out.println("\n  ── Trending Products ───────────────────────────────");
            printRecommendations(popularityEngine.recommend(uid, rated, 5));
        }
        System.out.println();
    }

    private static void menuHybrid(Scanner sc) {
        System.out.print("  Enter User ID (1-10): ");
        int userId = readInt(sc, 1, 10);
        System.out.println("  Custom weights? (y/n, default=n): ");
        String yn = sc.nextLine().trim();
        if ("y".equalsIgnoreCase(yn)) {
            System.out.print("  UserCF weight (e.g. 0.5): ");
            double w1 = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  ItemCF weight (e.g. 0.35): ");
            double w2 = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Popularity weight (e.g. 0.15): ");
            double w3 = Double.parseDouble(sc.nextLine().trim());
            hybridRecommender.setWeights(w1, w2, w3);
        }
        displayUserProfile(userId);
        System.out.println("\n  ── Hybrid Recommendations ──────────────────────────");
        Map<Integer, Double> rated = userRatings.getOrDefault(userId, new HashMap<>());
        printRecommendations(hybridRecommender.recommend(userId, rated, 5, "pearson"));
        System.out.println();
    }

    private static void menuProductDetails() {
        System.out.println("\n  ── Full Product Catalog ────────────────────────────");
        System.out.printf("  %-5s %-36s %-12s %8s  %s%n",
                "ID", "Name", "Category", "Price", "Avg Rating (# ratings)");
        System.out.println("  " + "─".repeat(80));
        for (Product p : productCatalog.values()) {
            double avg   = popularityEngine.getAverageRating(p.getId());
            int    count = popularityEngine.getRatingCount(p.getId());
            System.out.printf("  %-5d %-36s %-12s $%7.2f  %.1f★ (%d)%n",
                    p.getId(), p.getName(), p.getCategory(), p.getPrice(), avg, count);
        }
        System.out.println();
    }

    private static void menuUserProfile(Scanner sc) {
        System.out.print("  Enter User ID (1-10): ");
        int userId = readInt(sc, 1, 10);
        displayUserProfile(userId);
        System.out.println();
    }

    private static void menuDataStats() {
        System.out.println("\n  ── Dataset Statistics ──────────────────────────────");
        System.out.println("  Total Users   : " + userRatings.size());
        System.out.println("  Total Products: " + productCatalog.size());
        System.out.println("  Total Ratings : " + allRatings.size());
        double sparsity = 1.0 - (double) allRatings.size() / (userRatings.size() * productCatalog.size());
        System.out.printf("  Matrix Density: %.1f%% (Sparsity: %.1f%%)%n",
                (1 - sparsity) * 100, sparsity * 100);

        // Rating distribution
        Map<Integer, Integer> dist = new TreeMap<>();
        for (Rating r : allRatings) {
            int bucket = (int) r.getValue();
            dist.merge(bucket, 1, Integer::sum);
        }
        System.out.println("\n  Rating Distribution:");
        for (Map.Entry<Integer, Integer> e : dist.entrySet()) {
            int stars = e.getKey();
            int count = e.getValue();
            String bar = "█".repeat(count);
            System.out.printf("  %d★ : %-20s (%d)%n", stars, bar, count);
        }
        System.out.println();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static void displayUserProfile(int userId) {
        Map<Integer, Double> rated = userRatings.get(userId);
        System.out.println("\n  ── Profile: User " + userId + " ─────────────────────────────");
        if (rated == null || rated.isEmpty()) {
            System.out.println("  No ratings found for this user.");
            return;
        }
        System.out.printf("  %-38s %s%n", "Product", "Rating");
        System.out.println("  " + "─".repeat(50));
        for (Map.Entry<Integer, Double> e : rated.entrySet()) {
            Product p = productCatalog.get(e.getKey());
            String name = p != null ? p.getName() : "Product " + e.getKey();
            System.out.printf("  %-38s %.1f★%n", name, e.getValue());
        }
    }

    private static void printRecommendations(List<Recommendation> recs) {
        if (recs.isEmpty()) {
            System.out.println("│  No recommendations available.");
            return;
        }
        int rank = 1;
        for (Recommendation r : recs) {
            System.out.printf("│  #%d  %s%n", rank++, r);
        }
    }

    private static int readInt(Scanner sc, int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(sc.nextLine().trim());
                if (val >= min && val <= max) return val;
                System.out.print("  ⚠ Enter a number between " + min + " and " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("  ⚠ Invalid input. Try again: ");
            }
        }
    }

    private static void printMenu() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║         RECOMMENDATION SYSTEM MENU          ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  [1] User-Based Collaborative Filtering      ║");
        System.out.println("║  [2] Item-Based Collaborative Filtering      ║");
        System.out.println("║  [3] Popularity / Trending Products          ║");
        System.out.println("║  [4] Hybrid Recommender (All Combined)       ║");
        System.out.println("║  [5] Browse Product Catalog                  ║");
        System.out.println("║  [6] View User Rating Profile                ║");
        System.out.println("║  [7] Dataset Statistics                      ║");
        System.out.println("║  [0] Exit                                    ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║     Java AI-Based Product Recommendation System         ║");
        System.out.println("║     Algorithms: User-CF | Item-CF | Pop | Hybrid        ║");
        System.out.println("║     Data: 10 Users | 10 Products | 50 Ratings           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}