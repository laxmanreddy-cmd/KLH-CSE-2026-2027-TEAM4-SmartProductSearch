import java.util.*;

public class SmartProductSearch {

    // Product class
    static class Product {

        String name;
        String category;

        HashMap<String, Integer> prices;

        Product(String name, String category) {

            this.name = name;
            this.category = category;

            prices = new HashMap<>();
        }

        void addPrice(String platform, int price) {

            prices.put(platform, price);
        }
    }


    // List of products
    static ArrayList<Product> products = new ArrayList<>();


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // ---------------- PRODUCT DATA ----------------

        Product p1 = new Product(
            "iPhone 15",
            "Mobile"
        );

        p1.addPrice("Amazon", 65000);
        p1.addPrice("Flipkart", 64000);
        p1.addPrice("Myntra", 67000);


        Product p2 = new Product(
            "Samsung Galaxy S24",
            "Mobile"
        );

        p2.addPrice("Amazon", 72000);
        p2.addPrice("Flipkart", 70000);


        Product p3 = new Product(
            "Nike Shoes",
            "Fashion"
        );

        p3.addPrice("Amazon", 4500);
        p3.addPrice("Myntra", 4000);
        p3.addPrice("Flipkart", 4200);


        Product p4 = new Product(
            "Coca Cola",
            "Grocery"
        );

        p4.addPrice("Zepto", 40);
        p4.addPrice("Blinkit", 42);
        p4.addPrice("Amazon", 45);


        Product p5 = new Product(
            "Wireless Headphones",
            "Electronics"
        );

        p5.addPrice("Amazon", 2500);
        p5.addPrice("Flipkart", 2300);


        // Add products to ArrayList

        products.add(p1);
        products.add(p2);
        products.add(p3);
        products.add(p4);
        products.add(p5);


        // ---------------- MENU ----------------

        while (true) {

            System.out.println(
                "\n=============================================="
            );

            System.out.println(
                " SMART PRODUCT SEARCH & RECOMMENDATION SYSTEM"
            );

            System.out.println(
                "=============================================="
            );

            System.out.println("1. Search Product");
            System.out.println("2. Compare Price and Get Recommendation");
            System.out.println("3. Exit");

            System.out.print(
                "\nEnter your choice: "
            );

            String choice = scanner.nextLine();


            switch (choice) {


                // ==================================
                // CASE 1 - KMP PRODUCT SEARCH
                // ==================================

                case "1":

                    System.out.print(
                        "\nEnter product name to search: "
                    );

                    String search =
                        scanner.nextLine().toLowerCase();

                    boolean found = false;

                    System.out.println(
                        "\n----------- SEARCH RESULT -----------"
                    );


                    for (Product product : products) {

                        if (KMP(
                            product.name.toLowerCase(),
                            search
                        )) {

                            System.out.println(
                                "Product Found : YES"
                            );

                            System.out.println(
                                "Product Name  : "
                                + product.name
                            );

                            System.out.println(
                                "Category      : "
                                + product.category
                            );

                            found = true;

                            break;
                        }
                    }


                    if (!found) {

                        System.out.println(
                            "Product Found : NO"
                        );

                        System.out.println(
                            "No matching product available."
                        );
                    }

                    break;


                // ==================================
                // CASE 2 - PRICE COMPARISON
                // ==================================

                case "2":

                    System.out.print(
                        "\nEnter product name: "
                    );

                    String productName =
                        scanner.nextLine().toLowerCase();

                    Product selectedProduct = null;


                    for (Product product : products) {

                        if (product.name
                            .toLowerCase()
                            .equals(productName)) {

                            selectedProduct = product;

                            break;
                        }
                    }


                    if (selectedProduct == null) {

                        System.out.println(
                            "\nProduct Found : NO"
                        );

                        System.out.println(
                            "Please search for a valid product."
                        );
                    }

                    else {

                        System.out.println(
                            "\n----------- PRICE COMPARISON -----------"
                        );

                        System.out.println(
                            "Product : "
                            + selectedProduct.name
                        );

                        System.out.println(
                            "\nAvailable Prices:"
                        );


                        String recommendedPlatform = "";

                        int lowestPrice =
                            Integer.MAX_VALUE;


                        for (
                            Map.Entry<String, Integer> entry
                            : selectedProduct.prices.entrySet()
                        ) {

                            System.out.println(
                                entry.getKey()
                                + " : Rs."
                                + entry.getValue()
                            );


                            if (
                                entry.getValue()
                                < lowestPrice
                            ) {

                                lowestPrice =
                                    entry.getValue();

                                recommendedPlatform =
                                    entry.getKey();
                            }
                        }


                        System.out.println(
                            "\n----------- RECOMMENDATION -----------"
                        );

                        System.out.println(
                            "Recommended Platform : "
                            + recommendedPlatform
                        );

                        System.out.println(
                            "Best Price            : Rs."
                            + lowestPrice
                        );
                    }

                    break;


                // ==================================
                // CASE 3 - EXIT
                // ==================================

                case "3":

                    System.out.println(
                        "\nThank you for using Smart Product "
                        + "Search & Recommendation System."
                    );

                    scanner.close();

                    return;


                // ==================================
                // INVALID OPTION
                // ==================================

                default:

                    System.out.println(
                        "\nInvalid choice. "
                        + "Please enter 1, 2 or 3."
                    );
            }
        }
    }


    // ==============================================
    // KMP ALGORITHM
    // ==============================================

    public static boolean KMP(
        String text,
        String pattern
    ) {

        int[] lps =
            buildLPS(pattern);

        int i = 0;

        int j = 0;


        while (i < text.length()) {

            if (
                text.charAt(i)
                == pattern.charAt(j)
            ) {

                i++;

                j++;


                if (j == pattern.length()) {

                    return true;
                }
            }

            else {

                if (j != 0) {

                    j = lps[j - 1];
                }

                else {

                    i++;
                }
            }
        }

        return false;
    }


    // ==============================================
    // BUILD LPS ARRAY FOR KMP
    // ==============================================

    public static int[] buildLPS(
        String pattern
    ) {

        int[] lps =
            new int[pattern.length()];


        int length = 0;

        int i = 1;


        while (i < pattern.length()) {

            if (
                pattern.charAt(i)
                == pattern.charAt(length)
            ) {

                length++;

                lps[i] = length;

                i++;
            }

            else {

                if (length != 0) {

                    length =
                        lps[length - 1];
                }

                else {

                    lps[i] = 0;

                    i++;
                }
            }
        }

        return lps;
    }
}