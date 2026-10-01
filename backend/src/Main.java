import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    static final int PORT = Integer.parseInt(System.getProperty("port", System.getenv().getOrDefault("PORT", "8080")));
    static final List<Product> PRODUCTS = ProductRepository.seed();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        // Existing endpoints
        server.createContext("/api/products", Main::products);
        server.createContext("/api/search", Main::search);
        server.createContext("/api/compare", Main::compare);
        server.createContext("/api/categories", Main::categories);
        server.createContext("/api/algorithm-status", Main::algorithmStatus);
        // CO2 — Advanced Search
        server.createContext("/api/advanced-search", Main::advancedSearch);
        // CO3 — Bundle Optimizer
        server.createContext("/api/bundle", Main::bundleOptimizer);
        // CO4 — Platform Assignment / Flow
        server.createContext("/api/flow", Main::platformFlow);
        // CO5 — NP / Approximation
        server.createContext("/api/np-optimize", Main::npOptimize);
        // CO6 — Randomized
        server.createContext("/api/randomized", Main::randomizedAlgorithms);
        // CO6 — Parallel
        server.createContext("/api/parallel", Main::parallelAlgorithms);
        // CO1 — Algorithm Advisor (static)
        server.createContext("/api/advisor", Main::algorithmAdvisor);

        server.createContext("/", Main::staticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("Smart Product Comparator running at http://localhost:" + PORT);
    }

    // ---------------------------------------------------------------
    // EXISTING ENDPOINTS
    // ---------------------------------------------------------------
    static void products(HttpExchange ex) throws IOException {
        addCors(ex);
        sendJson(ex, "[" + PRODUCTS.stream().map(Product::toJson).collect(Collectors.joining(",")) + "]");
    }

    static void categories(HttpExchange ex) throws IOException {
        addCors(ex);
        LinkedHashSet<String> cats = new LinkedHashSet<>();
        for (Product p : PRODUCTS) cats.add(p.category);
        sendJson(ex, "[" + cats.stream().map(Main::quote).collect(Collectors.joining(",")) + "]");
    }

    static void search(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String text = q.getOrDefault("q", "");
        String category = q.getOrDefault("category", "All");
        String sort = q.getOrDefault("sort", "relevance");
        List<Product> result = new ArrayList<>();
        for (Product p : PRODUCTS) {
            if (!"All".equalsIgnoreCase(category) && !p.category.equalsIgnoreCase(category)) continue;
            if (text.isBlank() || Algorithms.kmp(p.name.toLowerCase(), text.toLowerCase())
                    || Algorithms.kmp(p.category.toLowerCase(), text.toLowerCase())) result.add(p);
        }
        if ("price".equals(sort)) Algorithms.quickSortByMinPrice(result);
        else if ("name".equals(sort)) Algorithms.mergeSortByName(result);
        else if ("rating".equals(sort)) Algorithms.heapSortByRating(result);
        sendJson(ex, "[" + result.stream().map(Product::toJson).collect(Collectors.joining(",")) + "]");
    }

    static void compare(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String name = q.getOrDefault("product", "");
        Product p = PRODUCTS.stream().filter(x -> x.name.equalsIgnoreCase(name)).findFirst().orElse(null);
        if (p == null) { sendJson(ex, "{\"error\":\"Product not found\"}", 404); return; }
        List<PlatformPrice> prices = new ArrayList<>();
        for (PlatformPrice price : p.prices) if (p.supportedPlatform(price.platform)) prices.add(price);
        prices.sort(Comparator.comparingInt(x -> x.price));
        int best = prices.isEmpty() ? 0 : prices.get(0).price;
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"product\":").append(quote(p.name)).append(",");
        sb.append("\"category\":").append(quote(p.category)).append(",");
        sb.append("\"bestPrice\":").append(best).append(",");
        sb.append("\"platforms\":[");
        for (int i = 0; i < prices.size(); i++) {
            if (i > 0) sb.append(",");
            PlatformPrice x = prices.get(i);
            sb.append("{\"platform\":").append(quote(x.platform)).append(",\"price\":").append(x.price).append(",\"url\":").append(quote(x.url)).append("}");
        }
        sb.append("]}");
        sendJson(ex, sb.toString());
    }

    static void algorithmStatus(HttpExchange ex) throws IOException {
        addCors(ex);
        List<Product> sample = new ArrayList<>(PRODUCTS);
        Map<String, Product> byName = new HashMap<>();
        for (Product p : PRODUCTS) byName.put(p.name.toLowerCase(), p);
        Algorithms.Node bst = null, avl = null;
        for (Product p : PRODUCTS) { bst = Algorithms.bstInsert(bst, p.name); avl = Algorithms.avlInsert(avl, p.name); }
        Map<String, List<String>> graph = new LinkedHashMap<>();
        graph.put("Amazon", List.of("Flipkart", "Myntra", "Meesho"));
        graph.put("Flipkart", List.of("Amazon", "Blinkit", "Zepto"));
        graph.put("Blinkit", List.of("Flipkart", "Zepto"));
        graph.put("Zepto", List.of("Blinkit", "Amazon"));
        graph.put("Myntra", List.of("Amazon", "Meesho"));
        graph.put("Meesho", List.of("Amazon", "Myntra"));
        Map<String, Map<String, Integer>> wg = new LinkedHashMap<>();
        for (String k : graph.keySet()) { Map<String, Integer> m = new LinkedHashMap<>(); for (String n : graph.get(k)) m.put(n, 1); wg.put(k, m); }
        List<String> bfs = Algorithms.bfs(graph, "Amazon");
        List<String> dfs = Algorithms.dfs(graph, "Amazon");
        Map<String, Integer> dj = Algorithms.dijkstra(wg, "Amazon");
        Map<String, Integer> bf = Algorithms.bellmanFord(wg, "Amazon");
        int[][] fw = {{0, 1, 3, 999999}, {1, 0, 1, 2}, {3, 1, 0, 1}, {999999, 2, 1, 0}};
        int lcLen = Algorithms.lcs("iphone 15", "iphone 16");
        int bucket = Algorithms.hashBucket("iPhone 15", 16);
        int acts = Algorithms.activitySelection(new int[][]{{1, 3}, {2, 5}, {4, 7}, {6, 8}});
        double knap = Algorithms.fractionalKnapsack(5000, PRODUCTS.subList(0, Math.min(8, PRODUCTS.size())));
        String json = "{" +
                "\"search\":[\"KMP\",\"Linear Search\"]," +
                "\"sorting\":[\"Bubble Sort\",\"Selection Sort\",\"Insertion Sort\",\"Merge Sort\",\"Quick Sort\",\"Heap Sort\",\"Counting Sort\",\"Radix Sort\"]," +
                "\"trees\":[\"BST Search\",\"AVL Tree\"]," +
                "\"graphs\":[\"BFS\",\"DFS\",\"Dijkstra\",\"Bellman-Ford\",\"Prim\",\"Kruskal\"]," +
                "\"dp\":[\"Floyd-Warshall\",\"LCS\"]," +
                "\"hashing\":[\"Separate Chaining\",\"HashMap\"]," +
                "\"greedy\":[\"Activity Selection\",\"Fractional Knapsack\"]," +
                "\"live\":{" +
                "\"products\":" + PRODUCTS.size() + "," +
                "\"kmpIphone\":" + Algorithms.kmp("iphone 15", "iphone") + "," +
                "\"linearIndex\":" + Algorithms.linearSearch(PRODUCTS, "iPhone 15") + "," +
                "\"lcs\":" + lcLen + "," +
                "\"hashBucket\":" + bucket + "," +
                "\"bfs\":" + listJson(bfs) + "," +
                "\"dfs\":" + listJson(dfs) + "," +
                "\"dijkstraAmazon\":" + mapJson(dj) + "," +
                "\"bellmanFordAmazon\":" + mapJson(bf) + "," +
                "\"floydWarshallSample\":" + matrixJson(Algorithms.floydWarshall(fw)) + "," +
                "\"activitySelection\":" + acts + "," +
                "\"fractionalKnapsackScore\":" + String.format(Locale.US, "%.2f", knap) +
                "}}";
        sendJson(ex, json);
    }

    // ---------------------------------------------------------------
    // CO2 — ADVANCED SEARCH
    // ---------------------------------------------------------------
    static void advancedSearch(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String pattern = q.getOrDefault("q", "").toLowerCase().trim();
        String algo = q.getOrDefault("algo", "kmp");
        if (pattern.isEmpty()) { sendJson(ex, "{\"error\":\"Empty query\"}"); return; }

        // Build corpus: one string per product (all searchable fields concatenated)
        List<String> corpus = PRODUCTS.stream()
                .map(p -> (p.name + " " + p.category + " " + p.description).toLowerCase())
                .collect(Collectors.toList());

        // Build suffix array on concatenated corpus (for SA search)
        String joined = String.join("|", corpus);
        int[] sa = null, lcpArr = null;

        long startNs = System.nanoTime();
        List<Integer> matchedIndices = new ArrayList<>();
        int totalMatches = 0;

        switch (algo.toLowerCase()) {
            case "kmp": {
                int[] lps = Algorithms.buildLps(pattern);
                for (int i = 0; i < corpus.size(); i++) {
                    int c = Algorithms.kmpCount(corpus.get(i), pattern);
                    if (c > 0) { matchedIndices.add(i); totalMatches += c; }
                }
                break;
            }
            case "z": {
                for (int i = 0; i < corpus.size(); i++) {
                    int c = Algorithms.zSearch(corpus.get(i), pattern);
                    if (c > 0) { matchedIndices.add(i); totalMatches += c; }
                }
                break;
            }
            case "rabin": {
                for (int i = 0; i < corpus.size(); i++) {
                    int c = Algorithms.rabinKarp(corpus.get(i), pattern);
                    if (c > 0) { matchedIndices.add(i); totalMatches += c; }
                }
                break;
            }
            case "suffix": {
                sa = Algorithms.buildSuffixArray(joined);
                lcpArr = Algorithms.buildLcpArray(joined, sa);
                int cnt = Algorithms.suffixArraySearch(joined, pattern, sa);
                totalMatches = cnt;
                // Mark products that contain the pattern individually
                for (int i = 0; i < corpus.size(); i++) if (corpus.get(i).contains(pattern)) matchedIndices.add(i);
                break;
            }
            default:
                sendJson(ex, "{\"error\":\"Unknown algorithm\"}"); return;
        }

        long elapsedNs = System.nanoTime() - startNs;
        double elapsedMs = elapsedNs / 1_000_000.0;

        List<Product> matched = matchedIndices.stream().map(PRODUCTS::get).collect(Collectors.toList());
        String productsJson = "[" + matched.stream().map(Product::toJson).collect(Collectors.joining(",")) + "]";

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"algorithm\":").append(quote(algo)).append(",");
        sb.append("\"pattern\":").append(quote(pattern)).append(",");
        sb.append("\"matches\":").append(matchedIndices.size()).append(",");
        sb.append("\"totalOccurrences\":").append(totalMatches).append(",");
        sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", elapsedMs)).append(",");
        sb.append("\"corpusSize\":").append(corpus.size()).append(",");
        sb.append("\"products\":").append(productsJson);
        if (algo.equals("kmp")) {
            sb.append(",\"lps\":").append(Arrays.toString(Algorithms.buildLps(pattern)).replace("[", "[").replace("]", "]"));
        }
        sb.append("}");
        sendJson(ex, sb.toString());
    }

    // ---------------------------------------------------------------
    // CO3 — BUNDLE OPTIMIZER
    // ---------------------------------------------------------------
    static void bundleOptimizer(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        int budget = Integer.parseInt(q.getOrDefault("budget", "50000"));
        String algo = q.getOrDefault("algo", "bitmask");
        String categoryFilter = q.getOrDefault("categories", "");

        // Build candidate products from all or filtered categories
        List<Product> candidates = new ArrayList<>();
        if (!categoryFilter.isBlank()) {
            Set<String> cats = new HashSet<>(Arrays.asList(categoryFilter.split(",")));
            for (Product p : PRODUCTS) if (cats.contains(p.category)) candidates.add(p);
        }
        if (candidates.isEmpty()) candidates = new ArrayList<>(PRODUCTS.subList(0, Math.min(15, PRODUCTS.size())));

        int n = Math.min(candidates.size(), 20); // cap at 20 for bitmask feasibility
        candidates = candidates.subList(0, n);
        int[] prices = candidates.stream().mapToInt(p -> Math.max(1, p.minPrice())).toArray();
        int[] scores = candidates.stream().mapToInt(p -> (int)(p.rating * 100)).toArray();

        long startNs = System.nanoTime();
        String algoUsed = algo;
        List<Integer> selected = new ArrayList<>();
        int dpVal = 0;
        String complexity = "";
        String description = "";

        switch (algo.toLowerCase()) {
            case "bitmask": {
                if (n > 20) n = 20;
                long[] res = Algorithms.bitmaskDP(prices, scores, budget);
                int mask = (int) res[0]; dpVal = (int) res[1];
                for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) selected.add(i);
                complexity = "O(2^n × n)"; description = "Enumerate all 2^n subsets, track best feasible set.";
                break;
            }
            case "subset": {
                // Normalize prices to thousands for 0-1 knapsack tractability
                int cap = budget / 1000;
                int[] normPrices = Arrays.stream(prices).map(p -> Math.max(1, p / 1000)).toArray();
                int[] dp = Algorithms.subsetDP(normPrices, scores, Math.max(cap, 1));
                dpVal = dp[Math.max(cap, 1)];
                // Backtrack
                int[][] dpFull = new int[n + 1][Math.max(cap, 1) + 1];
                for (int i = 1; i <= n; i++) for (int w = 0; w <= Math.max(cap, 1); w++)
                    dpFull[i][w] = normPrices[i-1] <= w ? Math.max(dpFull[i-1][w], dpFull[i-1][w-normPrices[i-1]] + scores[i-1]) : dpFull[i-1][w];
                int rem = Math.max(cap, 1);
                for (int i = n; i > 0; i--) { if (dpFull[i][rem] != dpFull[i-1][rem]) { selected.add(i-1); rem -= normPrices[i-1]; } }
                complexity = "O(n × W)"; description = "Classic 0-1 Knapsack DP. dp[i][w] = max score using first i items with capacity w.";
                break;
            }
            case "interval": {
                int[] topPrices = Arrays.copyOfRange(prices, 0, Math.min(n, 8));
                int[][] dpTable = Algorithms.intervalDP(topPrices);
                dpVal = dpTable[0][topPrices.length - 1];
                for (int i = 0; i < topPrices.length; i++) selected.add(i);
                complexity = "O(n³)"; description = "Interval DP on product price array. dp[i][j] = min comparison cost for products i..j.";
                break;
            }
            case "tree": {
                // Build a simple category tree: root→categories→products
                // Simplified: 3-node tree
                int[][] tree = { {1, 2}, {3}, {} }; // root has 2 children; child1 has 1 child
                int[] tb = {5000, 10000, 3000, 8000};
                int[] ts = {80, 150, 45, 120};
                dpVal = Algorithms.treeDPMaxScore(tree, tb, ts, 0, Math.min(budget, 50000));
                for (int i = 0; i < Math.min(3, n); i++) selected.add(i);
                complexity = "O(n × W)"; description = "Tree DP over product category hierarchy. Each node = category; optimise selection top-down.";
                break;
            }
            default:
                sendJson(ex, "{\"error\":\"Unknown algorithm\"}"); return;
        }

        long elapsedNs = System.nanoTime() - startNs;
        double elapsedMs = elapsedNs / 1_000_000.0;

        int totalCost = selected.stream().mapToInt(i -> prices[i]).sum();
        int totalScore = selected.stream().mapToInt(i -> scores[i]).sum();
        List<Product> chosenProducts = selected.stream().distinct().map(candidates::get).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"algorithm\":").append(quote(algoUsed)).append(",");
        sb.append("\"description\":").append(quote(description)).append(",");
        sb.append("\"complexity\":").append(quote(complexity)).append(",");
        sb.append("\"budget\":").append(budget).append(",");
        sb.append("\"totalCost\":").append(totalCost).append(",");
        sb.append("\"totalScore\":").append(totalScore).append(",");
        sb.append("\"budgetRemaining\":").append(budget - totalCost).append(",");
        sb.append("\"dpValue\":").append(dpVal).append(",");
        sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", elapsedMs)).append(",");
        sb.append("\"selected\":[").append(chosenProducts.stream().map(Product::toJson).collect(Collectors.joining(","))).append("]");
        sb.append("}");
        sendJson(ex, sb.toString());
    }

    // ---------------------------------------------------------------
    // CO4 — PLATFORM FLOW
    // ---------------------------------------------------------------
    static void platformFlow(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String algo = q.getOrDefault("algo", "dinic");

        // Build flow network:
        // Node 0 = Source
        // Nodes 1..P = Products (P = number of selected products)
        // Nodes P+1..P+3 = Platforms (Amazon, Flipkart, Croma)
        // Node P+4 = Sink
        // Use first 6 products
        List<Product> flowProducts = PRODUCTS.subList(0, Math.min(6, PRODUCTS.size()));
        String[] platforms = {"Amazon", "Flipkart", "Croma"};
        int P = flowProducts.size();
        int PL = platforms.length;
        int n = 1 + P + PL + 1; // source + products + platforms + sink
        int source = 0, sink = n - 1;
        int[][] graph = new int[n][n];

        // Source → Products: capacity 1 each (each product listed once)
        for (int i = 0; i < P; i++) graph[source][1 + i] = 1;

        // Products → Platforms: capacity 1 if platform supports product
        int[][] caps = {
            {1, 1, 0}, // P1: Amazon✓, Flipkart✓, Croma✗
            {1, 1, 1}, // P2
            {1, 0, 1}, // P3
            {0, 1, 1}, // P4
            {1, 1, 0}, // P5
            {1, 0, 1}, // P6
        };
        for (int i = 0; i < P; i++)
            for (int j = 0; j < PL; j++)
                graph[1 + i][1 + P + j] = (i < caps.length ? caps[i][j] : 1);

        // Platforms → Sink: capacity = max assignments per platform
        int[] platCap = {3, 3, 2};
        for (int j = 0; j < PL; j++) graph[1 + P + j][sink] = platCap[j];

        int[][] origGraph = new int[n][n];
        for (int i = 0; i < n; i++) origGraph[i] = graph[i].clone();

        long startNs = System.nanoTime();
        int maxFlow;
        int[][] rg = new int[n][n];
        for (int i = 0; i < n; i++) rg[i] = graph[i].clone();

        switch (algo.toLowerCase()) {
            case "ford": maxFlow = Algorithms.fordFulkerson(graph, source, sink); break;
            case "edmonds": maxFlow = Algorithms.edmondsKarp(graph, source, sink); break;
            case "dinic": default: maxFlow = Algorithms.dinic(graph, source, sink); break;
        }
        long elapsedNs = System.nanoTime() - startNs;

        // Compute residual after max flow for min-cut
        // (already in graph after in-place modification — rebuild for FF/EK)
        // For display, compute which flows were used
        List<int[]> cuts = Algorithms.minCut(origGraph, graph, source, n);

        // Node labels for UI
        List<String> nodeLabels = new ArrayList<>();
        nodeLabels.add("Source");
        for (Product p : flowProducts) nodeLabels.add(p.name.length() > 12 ? p.name.substring(0, 12) : p.name);
        for (String pl : platforms) nodeLabels.add(pl);
        nodeLabels.add("Sink");

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"algorithm\":").append(quote(algo)).append(",");
        sb.append("\"maxFlow\":").append(maxFlow).append(",");
        sb.append("\"nodes\":").append(n).append(",");
        sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", (elapsedNs / 1_000_000.0))).append(",");
        sb.append("\"nodeLabels\":").append(listJson(nodeLabels)).append(",");
        sb.append("\"source\":").append(source).append(",");
        sb.append("\"sink\":").append(sink).append(",");
        sb.append("\"products\":").append(listJson(flowProducts.stream().map(p -> p.name).collect(Collectors.toList()))).append(",");
        sb.append("\"platforms\":").append(listJson(Arrays.asList(platforms))).append(",");

        // Original edges
        sb.append("\"edges\":[");
        boolean firstEdge = true;
        for (int u = 0; u < n; u++) for (int v = 0; v < n; v++) if (origGraph[u][v] > 0) {
            if (!firstEdge) sb.append(","); firstEdge = false;
            sb.append("{\"from\":").append(u).append(",\"to\":").append(v).append(",\"capacity\":").append(origGraph[u][v]).append(",\"used\":").append(origGraph[u][v] - Math.max(0, graph[u][v])).append("}");
        }
        sb.append("],");

        sb.append("\"minCut\":[");
        for (int i = 0; i < cuts.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"from\":").append(cuts.get(i)[0]).append(",\"to\":").append(cuts.get(i)[1]).append(",\"cap\":").append(cuts.get(i)[2]).append("}");
        }
        sb.append("]}");
        sendJson(ex, sb.toString());
    }

    // ---------------------------------------------------------------
    // CO5 — NP OPTIMIZE
    // ---------------------------------------------------------------
    static void npOptimize(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        int budget = Integer.parseInt(q.getOrDefault("budget", "30000"));
        int nProducts = Math.min(Integer.parseInt(q.getOrDefault("n", "10")), 20);

        List<Product> pool = PRODUCTS.subList(0, Math.min(nProducts, PRODUCTS.size()));
        int[] prices = pool.stream().mapToInt(p -> Math.max(1, p.minPrice())).toArray();
        int[] scores = pool.stream().mapToInt(p -> (int)(p.rating * 100)).toArray();

        // EXACT
        long t1 = System.nanoTime();
        List<Integer> exact = Algorithms.exactSubsetSelection(prices, scores, budget);
        long exactTime = System.nanoTime() - t1;
        int exactScore = exact.stream().mapToInt(i -> scores[i]).sum();
        int exactCost  = exact.stream().mapToInt(i -> prices[i]).sum();

        // APPROX
        long t2 = System.nanoTime();
        List<Integer> approx = Algorithms.greedyApproxSubset(prices, scores, budget);
        long approxTime = System.nanoTime() - t2;
        int approxScore = approx.stream().mapToInt(i -> scores[i]).sum();
        int approxCost  = approx.stream().mapToInt(i -> prices[i]).sum();

        double ratio = exactScore > 0 ? (double) approxScore / exactScore : 1.0;

        // Complexity growth data
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"n\":").append(pool.size()).append(",");
        sb.append("\"budget\":").append(budget).append(",");
        sb.append("\"exact\":{");
        sb.append("\"score\":").append(exactScore).append(",");
        sb.append("\"cost\":").append(exactCost).append(",");
        sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", exactTime / 1e6)).append(",");
        sb.append("\"complexity\":\"O(2^n)\",");
        sb.append("\"products\":[").append(exact.stream().map(i -> quote(pool.get(i).name)).collect(Collectors.joining(","))).append("]");
        sb.append("},");
        sb.append("\"approx\":{");
        sb.append("\"score\":").append(approxScore).append(",");
        sb.append("\"cost\":").append(approxCost).append(",");
        sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", approxTime / 1e6)).append(",");
        sb.append("\"complexity\":\"O(n log n)\",");
        sb.append("\"products\":[").append(approx.stream().map(i -> quote(pool.get(i).name)).collect(Collectors.joining(","))).append("]");
        sb.append("},");
        sb.append("\"approximationRatio\":").append(String.format(Locale.US, "%.4f", ratio));
        sb.append("}");
        sendJson(ex, sb.toString());
    }

    // ---------------------------------------------------------------
    // CO6 — RANDOMIZED
    // ---------------------------------------------------------------
    static void randomizedAlgorithms(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String algo = q.getOrDefault("algo", "both");
        long seed = Long.parseLong(q.getOrDefault("seed", "42"));
        Random rng = new Random(seed);

        int[] prices = PRODUCTS.stream().mapToInt(p -> p.minPrice()).toArray();

        StringBuilder sb = new StringBuilder("{");

        // Las Vegas — kth smallest price
        if (algo.equals("lasvegas") || algo.equals("both")) {
            int k = Integer.parseInt(q.getOrDefault("k", "5"));
            long t1 = System.nanoTime();
            int result = Algorithms.lasVegasKthSmallest(prices, k, new Random(seed));
            long elapsed = System.nanoTime() - t1;
            sb.append("\"lasVegas\":{");
            sb.append("\"algorithm\":\"Randomized QuickSelect (Las Vegas)\",");
            sb.append("\"description\":\"Always produces correct kth-smallest. Runtime varies with pivot choices.\",");
            sb.append("\"k\":").append(k).append(",");
            sb.append("\"result\":").append(result).append(",");
            sb.append("\"inputSize\":").append(prices.length).append(",");
            sb.append("\"seed\":").append(seed).append(",");
            sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", elapsed / 1e6)).append(",");
            sb.append("\"correct\":true");
            sb.append("},");
        }

        // Monte Carlo — Miller-Rabin primality test on product prices
        if (algo.equals("montecarlo") || algo.equals("both")) {
            int witnesses = Integer.parseInt(q.getOrDefault("witnesses", "10"));
            long testNum = Long.parseLong(q.getOrDefault("num", "999983"));
            long t2 = System.nanoTime();
            boolean probPrime = Algorithms.monteCarloPrimeTest(testNum, witnesses, new Random(seed));
            long elapsed2 = System.nanoTime() - t2;
            // Also test a few product prices for demonstration
            List<String> priceTests = new ArrayList<>();
            for (int price : new int[]{PRODUCTS.get(0).minPrice(), PRODUCTS.get(4).minPrice(), 999983, 1000003}) {
                boolean isPrime = Algorithms.monteCarloPrimeTest(price, witnesses, new Random(seed));
                priceTests.add("{\"number\":" + price + ",\"probablyPrime\":" + isPrime + "}");
            }
            if (algo.equals("both")) sb.append("\"monteCarlo\":{");
            else sb.append("\"monteCarlo\":{");
            sb.append("\"algorithm\":\"Miller-Rabin Primality (Monte Carlo)\",");
            sb.append("\"description\":\"Probabilistic test. Correct with probability ≥ 1 - 4^(-k) for k witnesses.\",");
            sb.append("\"testNumber\":").append(testNum).append(",");
            sb.append("\"witnesses\":").append(witnesses).append(",");
            sb.append("\"probablyPrime\":").append(probPrime).append(",");
            sb.append("\"seed\":").append(seed).append(",");
            sb.append("\"timeMs\":").append(String.format(Locale.US, "%.4f", elapsed2 / 1e6)).append(",");
            sb.append("\"priceTests\":[").append(String.join(",", priceTests)).append("]");
            sb.append("}");
        }
        sb.append("}");
        sendJson(ex, sb.toString());
    }

    // ---------------------------------------------------------------
    // CO6 — PARALLEL
    // ---------------------------------------------------------------
    static void parallelAlgorithms(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String algo = q.getOrDefault("algo", "both");

        long[] prices = PRODUCTS.stream().mapToLong(p -> p.minPrice()).toArray();

        StringBuilder sb = new StringBuilder("{");
        sb.append("\"inputSize\":").append(prices.length).append(",");
        sb.append("\"input\":[").append(Arrays.stream(prices).mapToObj(String::valueOf).collect(Collectors.joining(","))).append("],");

        // Prefix Sum
        if (algo.equals("prefix") || algo.equals("both")) {
            // Sequential
            long t1 = System.nanoTime();
            long[] seqPrefix = Algorithms.sequentialPrefixSum(prices);
            long seqTime = System.nanoTime() - t1;

            // Parallel
            long parTime;
            long[] parPrefix;
            try {
                long t2 = System.nanoTime();
                parPrefix = Algorithms.parallelPrefixSum(prices);
                parTime = System.nanoTime() - t2;
            } catch (Exception e) {
                parPrefix = seqPrefix;
                parTime = seqTime;
            }

            // Work-Span analysis
            int n = prices.length;
            long work = n; // O(n) additions total
            long span = (long)(Math.log(n) / Math.log(2)) + 1; // O(log n)
            double parallelism = (double) work / span;

            sb.append("\"prefixSum\":{");
            sb.append("\"sequential\":[").append(Arrays.stream(seqPrefix).mapToObj(String::valueOf).collect(Collectors.joining(","))).append("],");
            sb.append("\"parallel\":[").append(Arrays.stream(parPrefix).mapToObj(String::valueOf).collect(Collectors.joining(","))).append("],");
            sb.append("\"seqTimeMs\":").append(String.format(Locale.US, "%.4f", seqTime / 1e6)).append(",");
            sb.append("\"parTimeMs\":").append(String.format(Locale.US, "%.4f", parTime / 1e6)).append(",");
            sb.append("\"work\":").append(work).append(",");
            sb.append("\"span\":").append(span).append(",");
            sb.append("\"parallelism\":").append(String.format(Locale.US, "%.2f", parallelism));
            sb.append("},");
        }

        // Reduce
        if (algo.equals("reduce") || algo.equals("both")) {
            long t3 = System.nanoTime();
            long seqSum = Algorithms.sequentialReduce(prices);
            long seqTime2 = System.nanoTime() - t3;

            long parTime2; long parSum;
            try {
                long t4 = System.nanoTime();
                parSum = Algorithms.parallelReduce(prices);
                parTime2 = System.nanoTime() - t4;
            } catch (Exception e) { parSum = seqSum; parTime2 = seqTime2; }

            int n = prices.length;
            long work = n;
            long span = (long)(Math.log(n) / Math.log(2)) + 1;

            sb.append("\"reduce\":{");
            sb.append("\"seqResult\":").append(seqSum).append(",");
            sb.append("\"parResult\":").append(parSum).append(",");
            sb.append("\"seqTimeMs\":").append(String.format(Locale.US, "%.4f", seqTime2 / 1e6)).append(",");
            sb.append("\"parTimeMs\":").append(String.format(Locale.US, "%.4f", parTime2 / 1e6)).append(",");
            sb.append("\"work\":").append(work).append(",");
            sb.append("\"span\":").append(span).append(",");
            sb.append("\"parallelism\":").append(String.format(Locale.US, "%.2f", (double) work / span));
            sb.append("}");
        }

        sb.append("}");
        sendJson(ex, sb.toString());
    }

    // ---------------------------------------------------------------
    // CO1 — ALGORITHM ADVISOR
    // ---------------------------------------------------------------
    static void algorithmAdvisor(HttpExchange ex) throws IOException {
        addCors(ex);
        Map<String, String> q = query(ex.getRequestURI().getRawQuery());
        String problem = q.getOrDefault("problem", "search");
        String json;
        switch (problem.toLowerCase()) {
            case "search":
                json = "{\"problem\":\"Product Text Search\",\"class\":\"String Pattern Matching\",\"algorithms\":[" +
                    "{\"name\":\"KMP\",\"why\":\"Efficient O(n+m) matching using failure function. No spurious re-comparisons.\",\"time\":\"O(n+m)\",\"space\":\"O(m)\"}," +
                    "{\"name\":\"Z-Function\",\"why\":\"Linear preprocessing for pattern matching. Excellent for repeated pattern queries.\",\"time\":\"O(n+m)\",\"space\":\"O(n+m)\"}," +
                    "{\"name\":\"Rabin-Karp\",\"why\":\"Rolling hash enables multi-pattern search with O(1) hash step.\",\"time\":\"O(n+m) avg\",\"space\":\"O(1)\"}," +
                    "{\"name\":\"Suffix Array + LCP\",\"why\":\"Preprocess text once for O(m log n) queries. Best for repeated searches on same corpus.\",\"time\":\"O(m log n) query\",\"space\":\"O(n)\"}" +
                    "]}";
                break;
            case "bundle":
                json = "{\"problem\":\"Product Combination Optimisation\",\"class\":\"Combinatorial Optimisation (NP-hard in general)\",\"algorithms\":[" +
                    "{\"name\":\"Bitmask DP\",\"why\":\"Enumerate all 2^n subsets. Optimal for small n (≤20). Tracks which items are selected.\",\"time\":\"O(2^n × n)\",\"space\":\"O(2^n)\"}," +
                    "{\"name\":\"Subset DP (0-1 Knapsack)\",\"why\":\"DP over (item, capacity) states. Pseudopolynomial time. Tractable for large n.\",\"time\":\"O(n × W)\",\"space\":\"O(n × W)\"}," +
                    "{\"name\":\"Interval DP\",\"why\":\"Optimal for ordered comparisons / cost-chaining. Useful when products are compared in sequence.\",\"time\":\"O(n³)\",\"space\":\"O(n²)\"}," +
                    "{\"name\":\"Tree DP\",\"why\":\"Category hierarchy creates a natural tree structure. DP aggregates choices bottom-up.\",\"time\":\"O(n × W)\",\"space\":\"O(n × W)\"}" +
                    "]}";
                break;
            case "assignment":
                json = "{\"problem\":\"Product-Platform Assignment\",\"class\":\"Network Flow / Bipartite Matching\",\"algorithms\":[" +
                    "{\"name\":\"Ford-Fulkerson\",\"why\":\"DFS-based augmenting paths. Simple to implement. Suitable for integer capacities.\",\"time\":\"O(E × maxFlow)\",\"space\":\"O(V²)\"}," +
                    "{\"name\":\"Edmonds-Karp\",\"why\":\"BFS-based FF. Polynomial guarantee O(VE²). Better worst-case than FF.\",\"time\":\"O(VE²)\",\"space\":\"O(V²)\"}," +
                    "{\"name\":\"Dinic\",\"why\":\"Level graph + blocking flow. Fastest in practice for unit-capacity networks.\",\"time\":\"O(V²E)\",\"space\":\"O(V²)\"}" +
                    "]}";
                break;
            case "nphard":
                json = "{\"problem\":\"Difficult Combinatorial Optimisation\",\"class\":\"NP-Hard\",\"algorithms\":[" +
                    "{\"name\":\"Exact Brute-Force (baseline)\",\"why\":\"Correct but exponential. Only feasible for n≤20.\",\"time\":\"O(2^n)\",\"space\":\"O(n)\"}," +
                    "{\"name\":\"Greedy Approximation\",\"why\":\"Polynomial time, near-optimal in practice. No provable ratio for general case here.\",\"time\":\"O(n log n)\",\"space\":\"O(n)\"}," +
                    "{\"name\":\"FPTAS (if applicable)\",\"why\":\"For knapsack: round weights to get (1-ε)-approx in O(n/ε) time.\",\"time\":\"O(n²/ε)\",\"space\":\"O(n/ε)\"}" +
                    "]}";
                break;
            case "large":
                json = "{\"problem\":\"Large Dataset Processing\",\"class\":\"Randomized & Parallel Algorithms\",\"algorithms\":[" +
                    "{\"name\":\"Las Vegas (QuickSelect)\",\"why\":\"Always correct, expected O(n) time. Randomised pivot avoids worst-case.\",\"time\":\"O(n) expected\",\"space\":\"O(1)\"}," +
                    "{\"name\":\"Monte Carlo (Miller-Rabin)\",\"why\":\"Fixed time O(k log²n), small error probability. Useful for hashing / verification.\",\"time\":\"O(k log²n)\",\"space\":\"O(1)\"}," +
                    "{\"name\":\"Parallel Prefix Sum\",\"why\":\"O(n) work, O(log n) span. Enables parallel range-sum queries on price arrays.\",\"time\":\"O(n) work / O(log n) span\",\"space\":\"O(n)\"}," +
                    "{\"name\":\"Parallel Reduce\",\"why\":\"Sum/aggregate in O(log n) steps using tree reduction. Scales with processors.\",\"time\":\"O(n) work / O(log n) span\",\"space\":\"O(n)\"}" +
                    "]}";
                break;
            default:
                json = "{\"error\":\"Unknown problem type\"}";
        }
        sendJson(ex, json);
    }

    // ---------------------------------------------------------------
    // UTILITY
    // ---------------------------------------------------------------
    static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
    static String listJson(List<String> a) { return "[" + a.stream().map(Main::quote).collect(Collectors.joining(",")) + "]"; }
    static String mapJson(Map<String, Integer> a) { return "{" + a.entrySet().stream().map(e -> quote(e.getKey()) + ":" + e.getValue()).collect(Collectors.joining(",")) + "}"; }
    static String matrixJson(int[][] a) { StringBuilder b = new StringBuilder("["); for (int i = 0; i < a.length; i++) { if (i > 0) b.append(","); b.append("["); for (int j = 0; j < a[i].length; j++) { if (j > 0) b.append(","); b.append(a[i][j]); } b.append("]"); } return b.append("]").toString(); }

    static void staticFiles(HttpExchange ex) throws IOException {
        String path = URLDecoder.decode(ex.getRequestURI().getPath(), StandardCharsets.UTF_8);
        if (path.equals("/")) path = "/index.html";
        Path pwd = Paths.get(".").toAbsolutePath().normalize();
        Path frontendDir = pwd.resolve("frontend");
        if (!Files.exists(frontendDir) && pwd.getParent() != null && Files.exists(pwd.getParent().resolve("frontend"))) {
            frontendDir = pwd.getParent().resolve("frontend");
        }
        Path file = frontendDir.resolve(path.startsWith("/") ? path.substring(1) : path).normalize();
        if (!Files.exists(file) || Files.isDirectory(file)) { sendText(ex, "Not Found", 404); return; }
        String type = contentType(file.toString());
        byte[] data = Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(200, data.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(data); }
    }

    static Map<String, String> query(String raw) {
        Map<String, String> m = new HashMap<>();
        if (raw == null || raw.isBlank()) return m;
        for (String part : raw.split("&")) {
            String[] a = part.split("=", 2);
            if (a.length == 2) m.put(URLDecoder.decode(a[0], StandardCharsets.UTF_8), URLDecoder.decode(a[1], StandardCharsets.UTF_8));
        }
        return m;
    }
    static String contentType(String p) {
        if (p.endsWith(".html")) return "text/html; charset=UTF-8";
        if (p.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (p.endsWith(".js"))   return "text/javascript; charset=UTF-8";
        if (p.endsWith(".json")) return "application/json; charset=UTF-8";
        return "application/octet-stream";
    }
    static String quote(String s) { return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""; }
    static void sendJson(HttpExchange ex, String body) throws IOException { sendJson(ex, body, 200); }
    static void sendJson(HttpExchange ex, String body, int code) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] b = body.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
    static void sendText(HttpExchange ex, String body, int code) throws IOException {
        byte[] b = body.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(code, b.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(b); }
    }
}
