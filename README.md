# 🛍️ Smart Product Search & Recommendation System

### *An Advanced Data Structures & Algorithms (DSA-3) Project*

**Project Members**

| Venkatesh Addanki | Pranav Chowdary | Laxman Reddy |
|:---:|:---:|:---:|
| 2520030404 | 2520030555 | 2520030097 |

**Project Guide:** Dr. S. Vinay Kumar

Fast product discovery • Price comparison • Algorithm demonstrations

**Course:** Data Structures and Algorithms–3 (25CS2103E)  
**Academic Year:** 2026–2027

---

## 📌 About the Project

Online shoppers often struggle to find products when they use partial names, search terms, or need to compare prices. The **Smart Product Search & Recommendation System** is a web-based project that demonstrates how advanced data structures and algorithms can make product search, sorting, comparison, and selection more efficient.

The application includes a browser-based interface, a Java backend, and a product catalogue. It also provides interactive algorithm modules organized around the **six Course Outcomes (CO1–CO6)** supplied for the DSA-3 course.

> **Data note:** The project report describes a catalogue of 24 products across 9 categories, with reference prices for Amazon and Flipkart. These are stored project values, not guaranteed live prices.

## ✨ Main Features

- **Product search:** Search the catalogue using keywords or partial product names.
- **Category filtering:** Narrow results by product category.
- **Sorting:** Arrange results by price, product name, or rating.
- **Price comparison:** Compare the listed Amazon and Flipkart prices for selected products.
- **Product shortlist:** Select products and view their prices side by side.
- **Advanced Algorithms Lab:** Explore the course-outcome modules and view algorithm results and execution-time measurements.

## 🧰 Technology Stack

| Part | Technology |
|---|---|
| Frontend | HTML5, CSS3, JavaScript |
| Backend | Java built-in HTTP server and REST-style API endpoints |
| Data exchange | JSON over HTTP |
| Runtime | JDK 11 or later |
| Tools | VS Code or IntelliJ IDEA, Git |

## 🧠 Course Syllabus: CO1–CO6

The following section follows the Course Outcome syllabus supplied for this project.

### CO1 — Problem Classes and Algorithm Selection
Evaluate problem-class signatures—substring search, sequence alignment, flow on a network, and NP-hard scheduling—and select an appropriate advanced-algorithm strategy.

### CO2 — Advanced String Algorithms
Apply linear-time string algorithms (**KMP, Z-function, Rabin–Karp with rolling hash**) and suffix-based structures (**suffix array, LCP array, suffix automaton at intuition level**) to solve large-scale pattern-matching problems.

### CO3 — Advanced Dynamic Programming
Apply advanced dynamic-programming patterns—**interval DP, bitmask DP, DP on trees, and DP on subsets**—to design polynomial-time algorithms for combinatorial optimisation.

### CO4 — Network Flow
Apply network-flow algorithms (**Ford–Fulkerson with Edmonds–Karp, and Dinic at intuition level**) and the **max-flow/min-cut duality** to model and solve assignment, matching, and capacity-constrained problems.

### CO5 — NP-Completeness and Approximation
Analyze NP-completeness—the classes **P, NP, NP-complete, and NP-hard**, and canonical reductions (**3-SAT → Vertex Cover → Independent Set**, etc.)—and design approximation algorithms with provable approximation ratios for selected NP-hard problems.

### CO6 — Randomized and Parallel Algorithms
Apply randomized algorithms (**Las Vegas, Monte Carlo**) and parallel-algorithm primitives (**parallel prefix sum, parallel reduce, work-span analysis**) to design algorithms whose performance benefits from randomness or multiple processors.

## 🏗️ Application Structure

The project follows a simple client–server structure:

1. **Presentation layer:** `index.html`, `styles.css`, and `app.js` provide the browser interface.
2. **Service layer:** `Main.java` starts the Java HTTP server and exposes API endpoints.
3. **Algorithm layer:** `Algorithms.java` contains the algorithm implementations.
4. **Product data layer:** `Product.java` and `ProductRepository.java` represent and supply the catalogue.

The exact filenames may vary if the supplied project folder has been reorganized.

## ▶️ Run the Project

### Requirements

- Install **JDK 11 or later**.
- Install **VS Code** (or use IntelliJ IDEA).
- Open the extracted project folder in your IDE.

### Start the application

1. Extract the project ZIP file.
2. Open the project folder in VS Code.
3. Confirm that a JDK is installed and available in the terminal:
   ```bash
   java -version
   javac -version
