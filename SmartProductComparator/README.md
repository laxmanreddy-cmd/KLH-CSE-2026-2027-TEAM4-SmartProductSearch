# Smart Product Search & Comparison System

A Java-only backend + vanilla HTML/CSS/JavaScript frontend project. The frontend is served directly by the Java HTTP server; there is no Node/Express backend. `npm run dev` is only the launcher command required by the submission.

## Run
1. Install JDK 17+ and Node.js/npm.
2. Open this folder in VS Code.
3. Run:
   ```bash
   npm run dev
   ```
4. Open http://localhost:8080

## Important
- Product prices in the built-in catalog are **demo/reference values**, not live prices.
- Every platform button is linked to a platform search/listing URL so the current price can be checked on the platform.
- No scraping or fake live-price claim is used.
- The application works without API keys or internet access after startup.

## Java algorithms
Algorithms are grouped in folders in completion order, with exactly two main algorithms per folder:
01_Searching: KMP, Linear Search
02_Basic_Sorting: Bubble Sort, Selection Sort
03_Sorting_Advanced: Insertion Sort, Merge Sort
04_Divide_Conquer: Quick Sort, Heap Sort
05_Non_Comparison_Sorting: Counting Sort, Radix Sort
06_Trees: BST Search, AVL Tree
07_Graph_Traversal: BFS, DFS
08_Shortest_Path: Dijkstra, Bellman-Ford
09_MST: Prim, Kruskal
10_Dynamic_Programming: Floyd-Warshall, LCS
11_Hashing: Separate Chaining, HashMap lookup
12_Greedy: Activity Selection, Fractional Knapsack

The web application uses these algorithms for real features such as product search, sorting, platform-price ranking, category navigation, recommendation scoring, and comparison.
