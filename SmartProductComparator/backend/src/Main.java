import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    static final int PORT = Integer.parseInt(System.getProperty("port", System.getenv().getOrDefault("PORT","8080")));
    static final List<Product> PRODUCTS = ProductRepository.seed();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/products", Main::products);
        server.createContext("/api/search", Main::search);
        server.createContext("/api/compare", Main::compare);
        server.createContext("/api/categories", Main::categories);
        server.createContext("/api/algorithm-status", Main::algorithmStatus);
        server.createContext("/", Main::staticFiles);
        server.setExecutor(null);
        server.start();
        System.out.println("Smart Product Comparator running at http://localhost:" + PORT);
    }

    static void products(HttpExchange ex) throws IOException {
        sendJson(ex, "[" + PRODUCTS.stream().map(Product::toJson).collect(Collectors.joining(",")) + "]");
    }

    static void categories(HttpExchange ex) throws IOException {
        LinkedHashSet<String> cats = new LinkedHashSet<>();
        for (Product p : PRODUCTS) cats.add(p.category);
        sendJson(ex, "[" + cats.stream().map(Main::quote).collect(Collectors.joining(",")) + "]");
    }

    static void search(HttpExchange ex) throws IOException {
        Map<String,String> q = query(ex.getRequestURI().getRawQuery());
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
        Map<String,String> q = query(ex.getRequestURI().getRawQuery());
        String name = q.getOrDefault("product", "");
        Product p = PRODUCTS.stream().filter(x -> x.name.equalsIgnoreCase(name)).findFirst().orElse(null);
        if (p == null) { sendJson(ex, "{\"error\":\"Product not found\"}", 404); return; }
        List<PlatformPrice> prices = new ArrayList<>(p.prices);
        prices.sort(Comparator.comparingInt(x -> x.price));
        int best = prices.isEmpty() ? 0 : prices.get(0).price;
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"product\":").append(quote(p.name)).append(",");
        sb.append("\"category\":").append(quote(p.category)).append(",");
        sb.append("\"bestPrice\":").append(best).append(",");
        sb.append("\"platforms\":[");
        for (int i=0;i<prices.size();i++) {
            if (i>0) sb.append(",");
            PlatformPrice x=prices.get(i);
            sb.append("{\"platform\":").append(quote(x.platform))
              .append(",\"price\":").append(x.price)
              .append(",\"url\":").append(quote(x.url)).append("}");
        }
        sb.append("]}");
        sendJson(ex, sb.toString());
    }


    static void algorithmStatus(HttpExchange ex) throws IOException {
        List<Product> sample = new ArrayList<>(PRODUCTS);
        Map<String,Product> byName = new HashMap<>();
        for(Product p: PRODUCTS) byName.put(p.name.toLowerCase(), p);
        Algorithms.Node bst=null, avl=null;
        for(Product p: PRODUCTS){ bst=Algorithms.bstInsert(bst,p.name); avl=Algorithms.avlInsert(avl,p.name); }
        Map<String,List<String>> graph=new LinkedHashMap<>();
        graph.put("Amazon",List.of("Flipkart","Myntra","Meesho"));
        graph.put("Flipkart",List.of("Amazon","Blinkit","Zepto"));
        graph.put("Blinkit",List.of("Flipkart","Zepto"));
        graph.put("Zepto",List.of("Blinkit","Amazon"));
        graph.put("Myntra",List.of("Amazon","Meesho"));
        graph.put("Meesho",List.of("Amazon","Myntra"));
        Map<String,Map<String,Integer>> wg=new LinkedHashMap<>();
        for(String k:graph.keySet()){Map<String,Integer> m=new LinkedHashMap<>();for(String n:graph.get(k))m.put(n,1);wg.put(k,m);}
        List<String> bfs=Algorithms.bfs(graph,"Amazon");
        List<String> dfs=Algorithms.dfs(graph,"Amazon");
        Map<String,Integer> dj=Algorithms.dijkstra(wg,"Amazon");
        Map<String,Integer> bf=Algorithms.bellmanFord(wg,"Amazon");
        int[][] fw={{0,1,3,999999},{1,0,1,2},{3,1,0,1},{999999,2,1,0}};
        int lcs=Algorithms.lcs("iphone 15","iphone 16");
        int bucket=Algorithms.hashBucket("iPhone 15",16);
        int acts=Algorithms.activitySelection(new int[][]{{1,3},{2,5},{4,7},{6,8}});
        double knap=Algorithms.fractionalKnapsack(5000,PRODUCTS.subList(0,Math.min(8,PRODUCTS.size())));
        String json="{"+
          "\"search\":[\"KMP\",\"Linear Search\"],"+
          "\"sorting\":[\"Bubble Sort\",\"Selection Sort\",\"Insertion Sort\",\"Merge Sort\",\"Quick Sort\",\"Heap Sort\",\"Counting Sort\",\"Radix Sort\"],"+
          "\"trees\":[\"BST Search\",\"AVL Tree\"],"+
          "\"graphs\":[\"BFS\",\"DFS\",\"Dijkstra\",\"Bellman-Ford\",\"Prim\",\"Kruskal\"],"+
          "\"dp\":[\"Floyd-Warshall\",\"LCS\"],"+
          "\"hashing\":[\"Separate Chaining\",\"HashMap\"],"+
          "\"greedy\":[\"Activity Selection\",\"Fractional Knapsack\"],"+
          "\"live\":{"+
          "\"products\":"+PRODUCTS.size()+","+
          "\"kmpIphone\":"+Algorithms.kmp("iphone 15","iphone")+","+
          "\"linearIndex\":"+Algorithms.linearSearch(PRODUCTS,"iPhone 15")+","+
          "\"lcs\":"+lcs+","+
          "\"hashBucket\":"+bucket+","+
          "\"bfs\":"+listJson(bfs)+","+
          "\"dfs\":"+listJson(dfs)+","+
          "\"dijkstraAmazon\":"+mapJson(dj)+","+
          "\"bellmanFordAmazon\":"+mapJson(bf)+","+
          "\"floydWarshallSample\":"+matrixJson(Algorithms.floydWarshall(fw))+","+
          "\"activitySelection\":"+acts+","+
          "\"fractionalKnapsackScore\":"+String.format(Locale.US,"%.2f",knap)+
          "}}";
        sendJson(ex,json);
    }
    static String listJson(List<String> a){return "["+a.stream().map(Main::quote).collect(Collectors.joining(","))+"]";}
    static String mapJson(Map<String,Integer> a){return "{"+a.entrySet().stream().map(e->quote(e.getKey())+":"+e.getValue()).collect(Collectors.joining(","))+"}";}
    static String matrixJson(int[][] a){StringBuilder b=new StringBuilder("[");for(int i=0;i<a.length;i++){if(i>0)b.append(",");b.append("[");for(int j=0;j<a[i].length;j++){if(j>0)b.append(",");b.append(a[i][j]);}b.append("]");}return b.append("]").toString();}

    static void staticFiles(HttpExchange ex) throws IOException {
        String path = URLDecoder.decode(ex.getRequestURI().getPath(), StandardCharsets.UTF_8);
        if (path.equals("/")) path="/frontend/index.html";
        else path="/frontend" + path;
        Path root=Paths.get(".").toAbsolutePath().normalize();
        Path file=root.resolve(path.substring(1)).normalize();
        if (!file.startsWith(root) || !Files.exists(file) || Files.isDirectory(file)) {
            sendText(ex, "Not Found", 404); return;
        }
        String type=contentType(file.toString());
        byte[] data=Files.readAllBytes(file);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(200,data.length);
        try(OutputStream os=ex.getResponseBody()){os.write(data);}
    }

    static Map<String,String> query(String raw) {
        Map<String,String> m=new HashMap<>();
        if(raw==null||raw.isBlank()) return m;
        for(String part:raw.split("&")){
            String[] a=part.split("=",2);
            if(a.length==2) m.put(URLDecoder.decode(a[0],StandardCharsets.UTF_8),URLDecoder.decode(a[1],StandardCharsets.UTF_8));
        }
        return m;
    }
    static String contentType(String p) {
        if(p.endsWith(".html")) return "text/html; charset=UTF-8";
        if(p.endsWith(".css")) return "text/css; charset=UTF-8";
        if(p.endsWith(".js")) return "text/javascript; charset=UTF-8";
        if(p.endsWith(".json")) return "application/json; charset=UTF-8";
        return "application/octet-stream";
    }
    static String quote(String s){ return "\"" + s.replace("\\","\\\\").replace("\"","\\\"") + "\""; }
    static void sendJson(HttpExchange ex,String body)throws IOException{sendJson(ex,body,200);}
    static void sendJson(HttpExchange ex,String body,int code)throws IOException{
        ex.getResponseHeaders().set("Content-Type","application/json; charset=UTF-8");
        byte[] b=body.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(code,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }
    static void sendText(HttpExchange ex,String body,int code)throws IOException{
        byte[] b=body.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(code,b.length);
        try(OutputStream os=ex.getResponseBody()){os.write(b);}
    }
}
