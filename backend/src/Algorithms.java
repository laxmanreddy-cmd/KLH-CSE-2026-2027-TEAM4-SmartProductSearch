import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

class Algorithms {

    // =========================================================
    // 01  SEARCHING  (existing)
    // =========================================================
    static boolean kmp(String text, String pattern) {
        if (pattern.isEmpty()) return true;
        int[] l = buildLps(pattern);
        int i = 0, j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) { i++; j++; if (j == pattern.length()) return true; }
            else if (j > 0) j = l[j - 1];
            else i++;
        }
        return false;
    }
    static int[] buildLps(String p) {
        int[] l = new int[p.length()];
        for (int i = 1, len = 0; i < p.length();) {
            if (p.charAt(i) == p.charAt(len)) l[i++] = ++len;
            else if (len > 0) len = l[len - 1];
            else l[i++] = 0;
        }
        return l;
    }
    static int linearSearch(List<Product> a, String name) {
        for (int i = 0; i < a.size(); i++) if (a.get(i).name.equalsIgnoreCase(name)) return i;
        return -1;
    }

    // =========================================================
    // CO2 — ADVANCED STRING SEARCH
    // =========================================================

    /** Returns count of non-overlapping occurrences of pattern in text using KMP. */
    static int kmpCount(String text, String pattern) {
        if (pattern.isEmpty()) return 0;
        int[] lps = buildLps(pattern);
        int count = 0, i = 0, j = 0;
        while (i < text.length()) {
            if (text.charAt(i) == pattern.charAt(j)) { i++; j++; }
            if (j == pattern.length()) { count++; j = lps[j - 1]; }
            else if (i < text.length() && text.charAt(i) != pattern.charAt(j)) {
                if (j != 0) j = lps[j - 1]; else i++;
            }
        }
        return count;
    }

    /** Z-function: z[i] = length of longest common prefix of text and text[i..] */
    static int[] zFunction(String s) {
        int n = s.length();
        int[] z = new int[n];
        z[0] = n;
        for (int i = 1, l = 0, r = 0; i < n; i++) {
            if (i < r) z[i] = Math.min(r - i, z[i - l]);
            while (i + z[i] < n && s.charAt(z[i]) == s.charAt(i + z[i])) z[i]++;
            if (i + z[i] > r) { l = i; r = i + z[i]; }
        }
        return z;
    }

    /** Z-function based pattern search: returns count of matches. */
    static int zSearch(String text, String pattern) {
        String concat = pattern + "$" + text;
        int[] z = zFunction(concat);
        int count = 0;
        for (int i = pattern.length() + 1; i < concat.length(); i++)
            if (z[i] == pattern.length()) count++;
        return count;
    }

    /** Rabin-Karp with rolling hash. Returns count of pattern occurrences. */
    static int rabinKarp(String text, String pattern) {
        int n = text.length(), m = pattern.length();
        if (m > n) return 0;
        final long BASE = 31, MOD = 1_000_000_007L;
        long patHash = 0, textHash = 0, power = 1;
        for (int i = 0; i < m - 1; i++) power = (power * BASE) % MOD;
        for (int i = 0; i < m; i++) {
            patHash  = (patHash  * BASE + (pattern.charAt(i) - 'a' + 1)) % MOD;
            textHash = (textHash * BASE + (text.charAt(i)    - 'a' + 1)) % MOD;
        }
        int count = 0;
        for (int i = 0; i <= n - m; i++) {
            if (patHash == textHash) {
                if (text.substring(i, i + m).equals(pattern)) count++;
            }
            if (i < n - m) {
                textHash = (BASE * (textHash - (text.charAt(i) - 'a' + 1) * power % MOD + MOD) + (text.charAt(i + m) - 'a' + 1)) % MOD;
            }
        }
        return count;
    }

    /** Build suffix array (O(n log n) prefix-doubling). */
    static int[] buildSuffixArray(String s) {
        int n = s.length();
        Integer[] sa = new Integer[n];
        int[] rank = new int[n], tmp = new int[n];
        for (int i = 0; i < n; i++) { sa[i] = i; rank[i] = s.charAt(i); }
        for (int gap = 1; gap < n; gap *= 2) {
            final int[] r = rank.clone(); final int g = gap;
            Arrays.sort(sa, (a, b) -> r[a] != r[b] ? r[a] - r[b] : (a + g < n ? r[a + g] : -1) - (b + g < n ? r[b + g] : -1));
            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                tmp[sa[i]] = tmp[sa[i-1]];
                int prev = sa[i-1], cur = sa[i];
                boolean diff = r[prev] != r[cur] || (prev+g < n ? r[prev+g] : -1) != (cur+g < n ? r[cur+g] : -1);
                if (diff) tmp[sa[i]]++;
            }
            rank = tmp.clone();
        }
        int[] result = new int[n];
        for (int i = 0; i < n; i++) result[i] = sa[i];
        return result;
    }

    /** Build LCP array using Kasai's algorithm. */
    static int[] buildLcpArray(String s, int[] sa) {
        int n = s.length();
        int[] rank = new int[n], lcp = new int[n];
        for (int i = 0; i < n; i++) rank[sa[i]] = i;
        for (int i = 0, h = 0; i < n; i++) {
            if (rank[i] > 0) {
                int j = sa[rank[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) h++;
                lcp[rank[i]] = h;
                if (h > 0) h--;
            }
        }
        return lcp;
    }

    /** Suffix array search: count occurrences of pattern in text. */
    static int suffixArraySearch(String text, String pattern, int[] sa) {
        int n = text.length(), m = pattern.length(), lo = 0, hi = n - 1, count = 0;
        // find left boundary
        int left = n;
        lo = 0; hi = n - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int cmp = text.substring(sa[mid], Math.min(sa[mid] + m, n)).compareTo(pattern);
            if (cmp < 0) lo = mid + 1; else { left = mid; hi = mid - 1; }
        }
        // find right boundary
        int right = -1;
        lo = 0; hi = n - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            int cmp = text.substring(sa[mid], Math.min(sa[mid] + m, n)).compareTo(pattern);
            if (cmp <= 0) { right = mid; lo = mid + 1; } else hi = mid - 1;
        }
        return (left <= right) ? right - left + 1 : 0;
    }

    // =========================================================
    // 02  BASIC SORTING  (existing)
    // =========================================================
    static void bubbleSortByPrice(List<Product> a) {
        for (int i = 0; i < a.size() - 1; i++)
            for (int j = 0; j < a.size() - i - 1; j++)
                if (a.get(j).minPrice() > a.get(j + 1).minPrice()) Collections.swap(a, j, j + 1);
    }
    static void selectionSortByPrice(List<Product> a) {
        for (int i = 0; i < a.size() - 1; i++) {
            int m = i;
            for (int j = i + 1; j < a.size(); j++) if (a.get(j).minPrice() < a.get(m).minPrice()) m = j;
            Collections.swap(a, i, m);
        }
    }

    // =========================================================
    // 03  ADVANCED SORTING  (existing)
    // =========================================================
    static void insertionSortByPrice(List<Product> a) {
        for (int i = 1; i < a.size(); i++) {
            Product key = a.get(i); int j = i - 1;
            while (j >= 0 && a.get(j).minPrice() > key.minPrice()) { a.set(j + 1, a.get(j)); j--; }
            a.set(j + 1, key);
        }
    }
    static void mergeSortByName(List<Product> a) { if (a.size() < 2) return; mergeName(a, 0, a.size() - 1); }
    static void mergeName(List<Product> a, int l, int r) {
        if (l >= r) return; int m = (l + r) / 2;
        mergeName(a, l, m); mergeName(a, m + 1, r);
        List<Product> t = new ArrayList<>(); int i = l, j = m + 1;
        while (i <= m && j <= r) { if (a.get(i).name.compareToIgnoreCase(a.get(j).name) <= 0) t.add(a.get(i++)); else t.add(a.get(j++)); }
        while (i <= m) t.add(a.get(i++)); while (j <= r) t.add(a.get(j++));
        for (int k = 0; k < t.size(); k++) a.set(l + k, t.get(k));
    }

    // =========================================================
    // 04  DIVIDE AND CONQUER  (existing)
    // =========================================================
    static void quickSortByMinPrice(List<Product> a) { quick(a, 0, a.size() - 1); }
    static void quick(List<Product> a, int l, int r) {
        if (l >= r) return;
        int i = l, j = r, p = a.get((l + r) / 2).minPrice();
        while (i <= j) { while (a.get(i).minPrice() < p) i++; while (a.get(j).minPrice() > p) j--; if (i <= j) { Collections.swap(a, i, j); i++; j--; } }
        if (l < j) quick(a, l, j); if (i < r) quick(a, i, r);
    }
    static void heapSortByRating(List<Product> a) {
        PriorityQueue<Product> pq = new PriorityQueue<>((x, y) -> Double.compare(y.rating, x.rating));
        pq.addAll(a); for (int i = 0; i < a.size(); i++) a.set(i, pq.poll());
    }

    // =========================================================
    // 05  NON-COMPARISON SORTING  (existing)
    // =========================================================
    static int[] countingSortPrices(List<Product> a) {
        int max = 0; for (Product p : a) max = Math.max(max, p.minPrice());
        int[] c = new int[Math.min(max + 1, 100001)];
        for (Product p : a) if (p.minPrice() < c.length) c[p.minPrice()]++;
        return c;
    }
    static int[] radixSortPrices(List<Product> a) {
        int[] v = a.stream().mapToInt(Product::minPrice).toArray();
        if (v.length == 0) return v;
        int max = Arrays.stream(v).max().orElse(0);
        for (int e = 1; max / e > 0; e *= 10) {
            int[] out = new int[v.length], c = new int[10];
            for (int n : v) c[(n / e) % 10]++;
            for (int i = 1; i < 10; i++) c[i] += c[i - 1];
            for (int i = v.length - 1; i >= 0; i--) { int n = v[i]; out[--c[(n / e) % 10]] = n; }
            v = out;
        }
        return v;
    }

    // =========================================================
    // 06  TREES  (existing)
    // =========================================================
    static class Node { String key; Node left, right; int h = 1; Node(String k) { key = k; } }
    static Node bstInsert(Node r, String k) {
        if (r == null) return new Node(k);
        if (k.compareToIgnoreCase(r.key) < 0) r.left = bstInsert(r.left, k);
        else if (k.compareToIgnoreCase(r.key) > 0) r.right = bstInsert(r.right, k);
        return r;
    }
    static boolean bstSearch(Node r, String k) {
        while (r != null) { int c = k.compareToIgnoreCase(r.key); if (c == 0) return true; r = c < 0 ? r.left : r.right; }
        return false;
    }
    static int height(Node n) { return n == null ? 0 : n.h; }
    static Node avlInsert(Node n, String k) {
        if (n == null) return new Node(k);
        if (k.compareToIgnoreCase(n.key) < 0) n.left = avlInsert(n.left, k);
        else if (k.compareToIgnoreCase(n.key) > 0) n.right = avlInsert(n.right, k);
        else return n;
        n.h = 1 + Math.max(height(n.left), height(n.right));
        int b = height(n.left) - height(n.right);
        if (b > 1 && k.compareToIgnoreCase(n.left.key) < 0) return rotR(n);
        if (b < -1 && k.compareToIgnoreCase(n.right.key) > 0) return rotL(n);
        if (b > 1) { n.left = rotL(n.left); return rotR(n); }
        if (b < -1) { n.right = rotR(n.right); return rotL(n); }
        return n;
    }
    static Node rotR(Node y) { Node x = y.left, t = x.right; x.right = y; y.left = t; y.h = 1 + Math.max(height(y.left), height(y.right)); x.h = 1 + Math.max(height(x.left), height(x.right)); return x; }
    static Node rotL(Node x) { Node y = x.right, t = y.left; y.left = x; x.right = t; x.h = 1 + Math.max(height(x.left), height(x.right)); y.h = 1 + Math.max(height(y.left), height(y.right)); return y; }

    // =========================================================
    // 07  GRAPH TRAVERSAL  (existing)
    // =========================================================
    static List<String> bfs(Map<String, List<String>> g, String start) {
        List<String> out = new ArrayList<>(); Queue<String> q = new ArrayDeque<>(); Set<String> v = new HashSet<>();
        q.add(start); v.add(start);
        while (!q.isEmpty()) { String u = q.poll(); out.add(u); for (String n : g.getOrDefault(u, List.of())) if (v.add(n)) q.add(n); }
        return out;
    }
    static void dfs0(Map<String, List<String>> g, String u, Set<String> v, List<String> o) { v.add(u); o.add(u); for (String n : g.getOrDefault(u, List.of())) if (!v.contains(n)) dfs0(g, n, v, o); }
    static List<String> dfs(Map<String, List<String>> g, String s) { List<String> o = new ArrayList<>(); dfs0(g, s, new HashSet<>(), o); return o; }

    // =========================================================
    // 08  SHORTEST PATH  (existing)
    // =========================================================
    static Map<String, Integer> dijkstra(Map<String, Map<String, Integer>> g, String s) {
        Map<String, Integer> d = new HashMap<>();
        for (String x : g.keySet()) d.put(x, Integer.MAX_VALUE); d.put(s, 0);
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(d::get)); pq.add(s);
        Set<String> done = new HashSet<>();
        while (!pq.isEmpty()) {
            String u = pq.poll(); if (!done.add(u)) continue;
            for (var e : g.getOrDefault(u, Map.of()).entrySet()) { int nd = d.get(u) + e.getValue(); if (nd < d.getOrDefault(e.getKey(), Integer.MAX_VALUE)) { d.put(e.getKey(), nd); pq.add(e.getKey()); } }
        }
        return d;
    }
    static Map<String, Integer> bellmanFord(Map<String, Map<String, Integer>> g, String s) {
        Map<String, Integer> d = new HashMap<>();
        for (String x : g.keySet()) d.put(x, Integer.MAX_VALUE); d.put(s, 0);
        for (int i = 1; i < g.size(); i++) for (String u : g.keySet()) for (var e : g.get(u).entrySet()) if (d.get(u) != Integer.MAX_VALUE) d.put(e.getKey(), Math.min(d.get(e.getKey()), d.get(u) + e.getValue()));
        return d;
    }

    // =========================================================
    // 09  MST  (existing)
    // =========================================================
    static int prim(Map<String, Map<String, Integer>> g) {
        if (g.isEmpty()) return 0;
        Set<String> v = new HashSet<>(); PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        String s = g.keySet().iterator().next(); v.add(s);
        for (var e : g.get(s).entrySet()) pq.add(new int[]{0, e.getValue()});
        int total = 0; while (!pq.isEmpty()) { int[] e = pq.poll(); total += e[1]; } return total;
    }
    static int kruskal(int[][] edges, int n) {
        Arrays.sort(edges, Comparator.comparingInt(e -> e[2])); int[] p = new int[n]; for (int i = 0; i < n; i++) p[i] = i;
        int total = 0, c = 0;
        for (int[] e : edges) { int a = find(p, e[0]), b = find(p, e[1]); if (a != b) { p[a] = b; total += e[2]; if (++c == n - 1) break; } } return total;
    }
    static int find(int[] p, int x) { return p[x] == x ? x : (p[x] = find(p, p[x])); }

    // =========================================================
    // 10  DP  (existing)
    // =========================================================
    static int[][] floydWarshall(int[][] d) {
        int n = d.length; int[][] a = new int[n][n]; for (int i = 0; i < n; i++) a[i] = d[i].clone();
        for (int k = 0; k < n; k++) for (int i = 0; i < n; i++) for (int j = 0; j < n; j++) if (a[i][k] < 1000000 && a[k][j] < 1000000) a[i][j] = Math.min(a[i][j], a[i][k] + a[k][j]);
        return a;
    }
    static int lcs(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) for (int j = 1; j <= b.length(); j++) dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1) ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        return dp[a.length()][b.length()];
    }

    // =========================================================
    // CO3 — DP ALGORITHMS
    // =========================================================

    /** Bitmask DP: select best subset of products within budget maximising total rating.
     *  products[i] = {price, score*100}. Returns {bestMask, bestScore, dpTable subset}. */
    static long[] bitmaskDP(int[] prices, int[] scores, int budget) {
        int n = prices.length;
        int masks = 1 << n;
        long[] dp = new long[masks]; // dp[mask] = max score within budget, -1 = infeasible
        Arrays.fill(dp, -1); dp[0] = 0;
        long bestScore = 0; int bestMask = 0;
        for (int mask = 1; mask < masks; mask++) {
            int cost = 0; long score = 0;
            for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) { cost += prices[i]; score += scores[i]; }
            if (cost <= budget) { dp[mask] = score; if (score > bestScore) { bestScore = score; bestMask = mask; } }
        }
        // return [bestMask, bestScore, n]
        return new long[]{bestMask, bestScore, n};
    }

    /** Subset DP (0-1 Knapsack): select items to maximise value within capacity.
     *  Returns dp table row for final result. */
    static int[] subsetDP(int[] weights, int[] values, int capacity) {
        int n = weights.length;
        int[][] dp = new int[n + 1][capacity + 1];
        for (int i = 1; i <= n; i++)
            for (int w = 0; w <= capacity; w++)
                dp[i][w] = (weights[i - 1] <= w) ? Math.max(dp[i - 1][w], dp[i - 1][w - weights[i - 1]] + values[i - 1]) : dp[i - 1][w];
        return dp[n]; // last row
    }

    /** Interval DP: optimal product chain comparison cost.
     *  Given products p1..pn, find minimum cost to sequentially compare all products
     *  where cost(i,j) = price[i] + price[j]. This models selecting a chain of comparisons.
     *  Returns the full DP table for visualization. */
    static int[][] intervalDP(int[] prices) {
        int n = prices.length;
        int[][] dp = new int[n][n];
        for (int len = 2; len <= n; len++)
            for (int i = 0; i <= n - len; i++) {
                int j = i + len - 1;
                dp[i][j] = Integer.MAX_VALUE;
                for (int k = i; k < j; k++) {
                    int cost = dp[i][k] + dp[k + 1][j] + prices[i] * prices[j] / 1000;
                    if (cost < dp[i][j]) dp[i][j] = cost;
                }
            }
        return dp;
    }

    /** Tree DP: given a category tree, select best products per category within budget.
     *  Simplified: each category node has children categories.
     *  Returns max score achievable. */
    static int treeDPMaxScore(int[][] tree, int[] budgets, int[] scores, int root, int totalBudget) {
        // tree[node] = list of children nodes (represented as adjacency list index)
        // Simple tree DP: dp[node][b] = max score using b budget in subtree rooted at node
        int n = budgets.length;
        int[][] dp = new int[n][totalBudget + 1];
        for (int[] row : dp) Arrays.fill(row, -1);
        return treeDPHelper(tree, budgets, scores, root, totalBudget, dp);
    }
    static int treeDPHelper(int[][] tree, int[] budgets, int[] scores, int node, int budget, int[][] dp) {
        if (dp[node][budget] != -1) return dp[node][budget];
        int best = 0;
        if (budgets[node] <= budget) best = scores[node];
        for (int child : tree[node]) {
            int childBest = treeDPHelper(tree, budgets, scores, child, budget - (budgets[node] <= budget ? budgets[node] : 0), dp);
            best = Math.max(best, (budgets[node] <= budget ? scores[node] : 0) + childBest);
        }
        dp[node][budget] = best;
        return best;
    }

    // =========================================================
    // CO4 — NETWORK FLOW ALGORITHMS
    // =========================================================

    /** Ford-Fulkerson with DFS augmenting paths. Returns max flow. */
    static int fordFulkerson(int[][] graph, int source, int sink) {
        int n = graph.length;
        int[][] rg = new int[n][n];
        for (int i = 0; i < n; i++) rg[i] = graph[i].clone();
        int maxFlow = 0;
        while (true) {
            int[] parent = new int[n]; Arrays.fill(parent, -1); parent[source] = source;
            Stack<Integer> stack = new Stack<>(); stack.push(source);
            outer: while (!stack.isEmpty()) {
                int u = stack.pop();
                for (int v = 0; v < n; v++) { if (parent[v] == -1 && rg[u][v] > 0) { parent[v] = u; stack.push(v); if (v == sink) break outer; } }
            }
            if (parent[sink] == -1) break;
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) { int u = parent[v]; pathFlow = Math.min(pathFlow, rg[u][v]); }
            for (int v = sink; v != source; v = parent[v]) { int u = parent[v]; rg[u][v] -= pathFlow; rg[v][u] += pathFlow; }
            maxFlow += pathFlow;
        }
        return maxFlow;
    }

    /** Edmonds-Karp (BFS-based Ford-Fulkerson). Returns max flow. */
    static int edmondsKarp(int[][] graph, int source, int sink) {
        int n = graph.length;
        int[][] rg = new int[n][n];
        for (int i = 0; i < n; i++) rg[i] = graph[i].clone();
        int maxFlow = 0;
        while (true) {
            int[] parent = new int[n]; Arrays.fill(parent, -1); parent[source] = source;
            Queue<Integer> queue = new ArrayDeque<>(); queue.add(source);
            while (!queue.isEmpty() && parent[sink] == -1) {
                int u = queue.poll();
                for (int v = 0; v < n; v++) { if (parent[v] == -1 && rg[u][v] > 0) { parent[v] = u; queue.add(v); } }
            }
            if (parent[sink] == -1) break;
            int pathFlow = Integer.MAX_VALUE;
            for (int v = sink; v != source; v = parent[v]) { int u = parent[v]; pathFlow = Math.min(pathFlow, rg[u][v]); }
            for (int v = sink; v != source; v = parent[v]) { int u = parent[v]; rg[u][v] -= pathFlow; rg[v][u] += pathFlow; }
            maxFlow += pathFlow;
        }
        return maxFlow;
    }

    /** Dinic's algorithm. Returns max flow. */
    static int dinic(int[][] graph, int source, int sink) {
        int n = graph.length;
        int[][] rg = new int[n][n];
        for (int i = 0; i < n; i++) rg[i] = graph[i].clone();
        int maxFlow = 0;
        int[] level = new int[n];
        while (dinicBFS(rg, source, sink, level, n)) {
            int[] iter = new int[n];
            int f;
            while ((f = dinicDFS(rg, source, sink, Integer.MAX_VALUE, level, iter, n)) > 0) maxFlow += f;
        }
        return maxFlow;
    }
    static boolean dinicBFS(int[][] rg, int s, int t, int[] level, int n) {
        Arrays.fill(level, -1); level[s] = 0;
        Queue<Integer> q = new ArrayDeque<>(); q.add(s);
        while (!q.isEmpty()) { int u = q.poll(); for (int v = 0; v < n; v++) if (level[v] < 0 && rg[u][v] > 0) { level[v] = level[u] + 1; q.add(v); } }
        return level[t] >= 0;
    }
    static int dinicDFS(int[][] rg, int u, int t, int pushed, int[] level, int[] iter, int n) {
        if (u == t) return pushed;
        for (; iter[u] < n; iter[u]++) {
            int v = iter[u];
            if (level[v] != level[u] + 1 || rg[u][v] == 0) continue;
            int d = dinicDFS(rg, v, t, Math.min(pushed, rg[u][v]), level, iter, n);
            if (d > 0) { rg[u][v] -= d; rg[v][u] += d; return d; }
        }
        return 0;
    }

    /** Compute min-cut after max flow: returns list of edges in the min-cut. */
    static List<int[]> minCut(int[][] original, int[][] rg, int source, int n) {
        // Find all nodes reachable from source in residual graph
        boolean[] visited = new boolean[n];
        Queue<Integer> q = new ArrayDeque<>(); q.add(source); visited[source] = true;
        while (!q.isEmpty()) { int u = q.poll(); for (int v = 0; v < n; v++) if (!visited[v] && rg[u][v] > 0) { visited[v] = true; q.add(v); } }
        List<int[]> cuts = new ArrayList<>();
        for (int u = 0; u < n; u++) for (int v = 0; v < n; v++) if (visited[u] && !visited[v] && original[u][v] > 0) cuts.add(new int[]{u, v, original[u][v]});
        return cuts;
    }

    // =========================================================
    // CO5 — NP / APPROXIMATION
    // =========================================================

    /** Brute-force exact subset selection maximising score within budget.
     *  NP-hard for general case. Returns best subset indices. */
    static List<Integer> exactSubsetSelection(int[] prices, int[] scores, int budget) {
        int n = prices.length;
        int bestScore = 0; int bestMask = 0;
        for (int mask = 0; mask < (1 << n); mask++) {
            int cost = 0, score = 0;
            for (int i = 0; i < n; i++) if ((mask & (1 << i)) != 0) { cost += prices[i]; score += scores[i]; }
            if (cost <= budget && score > bestScore) { bestScore = score; bestMask = mask; }
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < n; i++) if ((bestMask & (1 << i)) != 0) result.add(i);
        return result;
    }

    /** Greedy approximation for subset selection (ratio heuristic).
     *  Sort by score/price ratio. Returns selected indices. */
    static List<Integer> greedyApproxSubset(int[] prices, int[] scores, int budget) {
        Integer[] idx = new Integer[prices.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        Arrays.sort(idx, (a, b) -> Double.compare((double) scores[b] / prices[b], (double) scores[a] / prices[a]));
        List<Integer> result = new ArrayList<>(); int rem = budget;
        for (int i : idx) { if (prices[i] <= rem) { result.add(i); rem -= prices[i]; } }
        return result;
    }

    // =========================================================
    // CO6 — RANDOMIZED ALGORITHMS
    // =========================================================

    /** Las Vegas: randomized quickselect — always correct, runtime varies.
     *  Returns the k-th smallest price (0-indexed). */
    static int lasVegasKthSmallest(int[] arr, int k, Random rng) {
        int[] a = arr.clone();
        return quickSelect(a, 0, a.length - 1, k, rng);
    }
    static int quickSelect(int[] a, int lo, int hi, int k, Random rng) {
        if (lo == hi) return a[lo];
        int pivotIdx = lo + rng.nextInt(hi - lo + 1);
        int pivot = a[pivotIdx]; a[pivotIdx] = a[hi]; a[hi] = pivot;
        int i = lo;
        for (int j = lo; j < hi; j++) if (a[j] <= a[hi]) { int tmp = a[i]; a[i] = a[j]; a[j] = tmp; i++; }
        int tmp = a[i]; a[i] = a[hi]; a[hi] = tmp;
        if (k == i) return a[i];
        else if (k < i) return quickSelect(a, lo, i - 1, k, rng);
        else return quickSelect(a, i + 1, hi, k, rng);
    }

    /** Monte Carlo: probabilistic primality test (Miller-Rabin).
     *  Returns true if n is probably prime. k = number of witnesses. */
    static boolean monteCarloPrimeTest(long n, int k, Random rng) {
        if (n < 2) return false; if (n == 2 || n == 3) return true; if (n % 2 == 0) return false;
        long d = n - 1; int r = 0; while (d % 2 == 0) { d /= 2; r++; }
        for (int i = 0; i < k; i++) {
            long a = 2 + (Math.abs(rng.nextLong()) % (n - 3));
            long x = modPow(a, d, n);
            if (x == 1 || x == n - 1) continue;
            boolean composite = true;
            for (int j = 0; j < r - 1; j++) { x = modPow(x, 2, n); if (x == n - 1) { composite = false; break; } }
            if (composite) return false;
        }
        return true;
    }
    static long modPow(long base, long exp, long mod) {
        long result = 1; base %= mod;
        while (exp > 0) { if ((exp & 1) == 1) result = result * base % mod; exp >>= 1; base = base * base % mod; }
        return result;
    }

    // =========================================================
    // CO6 — PARALLEL ALGORITHMS
    // =========================================================

    /** Sequential prefix sum. */
    static long[] sequentialPrefixSum(long[] arr) {
        long[] result = new long[arr.length];
        if (arr.length == 0) return result;
        result[0] = arr[0];
        for (int i = 1; i < arr.length; i++) result[i] = result[i - 1] + arr[i];
        return result;
    }

    /** Parallel prefix sum using ForkJoinPool (work-efficient). */
    static long[] parallelPrefixSum(long[] arr) throws Exception {
        if (arr.length == 0) return new long[0];
        long[] result = arr.clone();
        ForkJoinPool pool = ForkJoinPool.commonPool();
        pool.submit(() -> {
            // Up-sweep (reduce) phase
            for (int stride = 1; stride < result.length; stride *= 2) {
                final int s = stride;
                int[] indices = new int[(result.length / (2 * s))];
                for (int i = 0; i < indices.length; i++) indices[i] = (2 * s * (i + 1)) - 1;
                Arrays.stream(indices).parallel().forEach(i -> { if (i < result.length) result[i] += result[i - s]; });
            }
            // Down-sweep phase
            result[result.length - 1] = 0;
            for (int stride = Integer.highestOneBit(result.length - 1); stride > 0; stride /= 2) {
                final int s = stride;
                int[] indices = new int[(result.length / (2 * s))];
                for (int i = 0; i < indices.length; i++) indices[i] = (2 * s * (i + 1)) - 1;
                Arrays.stream(indices).parallel().forEach(i -> {
                    if (i < result.length) { long t = result[i - s]; result[i - s] = result[i]; result[i] += t; }
                });
            }
        }).get();
        // Convert exclusive to inclusive
        for (int i = result.length - 1; i > 0; i--) result[i] = result[i - 1] + arr[i];
        result[0] = arr[0];
        return result;
    }

    /** Sequential reduce (sum). */
    static long sequentialReduce(long[] arr) {
        long sum = 0; for (long v : arr) sum += v; return sum;
    }

    /** Parallel reduce using ForkJoinPool. */
    static long parallelReduce(long[] arr) throws Exception {
        if (arr.length == 0) return 0;
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return pool.submit(() -> Arrays.stream(arr).parallel().sum()).get();
    }

    // =========================================================
    // 11  HASHING  (existing)
    // =========================================================
    static int hashBucket(String key, int size) { return Math.floorMod(key.hashCode(), size); }
    static String hashLookup(Map<String, Product> map, String key) { Product p = map.get(key.toLowerCase()); return p == null ? null : p.name; }

    // =========================================================
    // 12  GREEDY  (existing)
    // =========================================================
    static int activitySelection(int[][] acts) {
        Arrays.sort(acts, Comparator.comparingInt(a -> a[1])); int count = 0, last = -1;
        for (int[] a : acts) if (a[0] >= last) { count++; last = a[1]; } return count;
    }
    static double fractionalKnapsack(int budget, List<Product> a) {
        List<Product> copy = new ArrayList<>(a); copy.sort((x, y) -> Double.compare(y.rating / Math.max(1, y.minPrice()), x.rating / Math.max(1, x.minPrice())));
        double value = 0, rem = budget;
        for (Product p : copy) { if (rem <= 0) break; double take = Math.min(rem, p.minPrice()); value += take * (p.rating / 5.0); rem -= take; } return value;
    }
}
