import java.util.*;
class Algorithms {
    // 01 Searching
    static boolean kmp(String text,String pattern){ if(pattern.isEmpty())return true; int[] l=buildLps(pattern); int i=0,j=0; while(i<text.length()){if(text.charAt(i)==pattern.charAt(j)){i++;j++;if(j==pattern.length())return true;}else if(j>0)j=l[j-1];else i++;}return false;}
    static int[] buildLps(String p){int[] l=new int[p.length()];for(int i=1,len=0;i<p.length();){if(p.charAt(i)==p.charAt(len))l[i++]=++len;else if(len>0)len=l[len-1];else l[i++]=0;}return l;}
    static int linearSearch(List<Product> a,String name){for(int i=0;i<a.size();i++)if(a.get(i).name.equalsIgnoreCase(name))return i;return -1;}

    // 02 Basic sorting
    static void bubbleSortByPrice(List<Product> a){for(int i=0;i<a.size()-1;i++)for(int j=0;j<a.size()-i-1;j++)if(a.get(j).minPrice()>a.get(j+1).minPrice())Collections.swap(a,j,j+1);}
    static void selectionSortByPrice(List<Product> a){for(int i=0;i<a.size()-1;i++){int m=i;for(int j=i+1;j<a.size();j++)if(a.get(j).minPrice()<a.get(m).minPrice())m=j;Collections.swap(a,i,m);}}

    // 03 Advanced sorting
    static void insertionSortByPrice(List<Product> a){for(int i=1;i<a.size();i++){Product key=a.get(i);int j=i-1;while(j>=0&&a.get(j).minPrice()>key.minPrice()){a.set(j+1,a.get(j));j--;}a.set(j+1,key);}}
    static void mergeSortByName(List<Product> a){if(a.size()<2)return;mergeName(a,0,a.size()-1);}
    static void mergeName(List<Product>a,int l,int r){if(l>=r)return;int m=(l+r)/2;mergeName(a,l,m);mergeName(a,m+1,r);List<Product> t=new ArrayList<>();int i=l,j=m+1;while(i<=m&&j<=r){if(a.get(i).name.compareToIgnoreCase(a.get(j).name)<=0)t.add(a.get(i++));else t.add(a.get(j++));}while(i<=m)t.add(a.get(i++));while(j<=r)t.add(a.get(j++));for(int k=0;k<t.size();k++)a.set(l+k,t.get(k));}

    // 04 Divide and conquer
    static void quickSortByMinPrice(List<Product>a){quick(a,0,a.size()-1);}
    static void quick(List<Product>a,int l,int r){if(l>=r)return;int i=l,j=r,p=a.get((l+r)/2).minPrice();while(i<=j){while(a.get(i).minPrice()<p)i++;while(a.get(j).minPrice()>p)j--;if(i<=j){Collections.swap(a,i,j);i++;j--;}}if(l<j)quick(a,l,j);if(i<r)quick(a,i,r);}
    static void heapSortByRating(List<Product>a){PriorityQueue<Product> pq=new PriorityQueue<>((x,y)->Double.compare(y.rating,x.rating));pq.addAll(a);for(int i=0;i<a.size();i++)a.set(i,pq.poll());}

    // 05 Non-comparison sorting
    static int[] countingSortPrices(List<Product>a){int max=0;for(Product p:a)max=Math.max(max,p.minPrice());int[] c=new int[Math.min(max+1,100001)];for(Product p:a)if(p.minPrice()<c.length)c[p.minPrice()]++;return c;}
    static int[] radixSortPrices(List<Product>a){int[] v=a.stream().mapToInt(Product::minPrice).toArray();if(v.length==0)return v;int max=Arrays.stream(v).max().orElse(0);for(int e=1;max/e>0;e*=10){int[] out=new int[v.length],c=new int[10];for(int n:v)c[(n/e)%10]++;for(int i=1;i<10;i++)c[i]+=c[i-1];for(int i=v.length-1;i>=0;i--){int n=v[i];out[--c[(n/e)%10]]=n;}v=out;}return v;}

    // 06 Trees
    static class Node{String key;Node left,right;int h=1;Node(String k){key=k;}}
    static Node bstInsert(Node r,String k){if(r==null)return new Node(k);if(k.compareToIgnoreCase(r.key)<0)r.left=bstInsert(r.left,k);else if(k.compareToIgnoreCase(r.key)>0)r.right=bstInsert(r.right,k);return r;}
    static boolean bstSearch(Node r,String k){while(r!=null){int c=k.compareToIgnoreCase(r.key);if(c==0)return true;r=c<0?r.left:r.right;}return false;}
    static int height(Node n){return n==null?0:n.h;} static Node avlInsert(Node n,String k){if(n==null)return new Node(k);if(k.compareToIgnoreCase(n.key)<0)n.left=avlInsert(n.left,k);else if(k.compareToIgnoreCase(n.key)>0)n.right=avlInsert(n.right,k);else return n;n.h=1+Math.max(height(n.left),height(n.right));int b=height(n.left)-height(n.right);if(b>1&&k.compareToIgnoreCase(n.left.key)<0)return rotR(n);if(b<-1&&k.compareToIgnoreCase(n.right.key)>0)return rotL(n);if(b>1){n.left=rotL(n.left);return rotR(n);}if(b<-1){n.right=rotR(n.right);return rotL(n);}return n;}
    static Node rotR(Node y){Node x=y.left,t=x.right;x.right=y;y.left=t;y.h=1+Math.max(height(y.left),height(y.right));x.h=1+Math.max(height(x.left),height(x.right));return x;}
    static Node rotL(Node x){Node y=x.right,t=y.left;y.left=x;x.right=t;x.h=1+Math.max(height(x.left),height(x.right));y.h=1+Math.max(height(y.left),height(y.right));return y;}

    // 07 Graph traversal
    static List<String> bfs(Map<String,List<String>> g,String start){List<String> out=new ArrayList<>();Queue<String>q=new ArrayDeque<>();Set<String>v=new HashSet<>();q.add(start);v.add(start);while(!q.isEmpty()){String u=q.poll();out.add(u);for(String n:g.getOrDefault(u,List.of()))if(v.add(n))q.add(n);}return out;}
    static void dfs0(Map<String,List<String>>g,String u,Set<String>v,List<String>o){v.add(u);o.add(u);for(String n:g.getOrDefault(u,List.of()))if(!v.contains(n))dfs0(g,n,v,o);}
    static List<String> dfs(Map<String,List<String>>g,String s){List<String>o=new ArrayList<>();dfs0(g,s,new HashSet<>(),o);return o;}

    // 08 Shortest path
    static Map<String,Integer> dijkstra(Map<String,Map<String,Integer>>g,String s){Map<String,Integer>d=new HashMap<>();for(String x:g.keySet())d.put(x,Integer.MAX_VALUE);d.put(s,0);PriorityQueue<String>pq=new PriorityQueue<>(Comparator.comparingInt(d::get));pq.add(s);Set<String>done=new HashSet<>();while(!pq.isEmpty()){String u=pq.poll();if(!done.add(u))continue;for(var e:g.getOrDefault(u,Map.of()).entrySet()){int nd=d.get(u)+e.getValue();if(nd<d.getOrDefault(e.getKey(),Integer.MAX_VALUE)){d.put(e.getKey(),nd);pq.add(e.getKey());}}}return d;}
    static Map<String,Integer> bellmanFord(Map<String,Map<String,Integer>>g,String s){Map<String,Integer>d=new HashMap<>();for(String x:g.keySet())d.put(x,Integer.MAX_VALUE);d.put(s,0);for(int i=1;i<g.size();i++)for(String u:g.keySet())for(var e:g.get(u).entrySet())if(d.get(u)!=Integer.MAX_VALUE)d.put(e.getKey(),Math.min(d.get(e.getKey()),d.get(u)+e.getValue()));return d;}

    // 09 Minimum spanning tree
    static int prim(Map<String,Map<String,Integer>>g){if(g.isEmpty())return 0;Set<String>v=new HashSet<>();PriorityQueue<int[]>pq=new PriorityQueue<>(Comparator.comparingInt(a->a[1]));String s=g.keySet().iterator().next();v.add(s);for(var e:g.get(s).entrySet())pq.add(new int[]{0,e.getValue()});int total=0;while(!pq.isEmpty()){int[]e=pq.poll();total+=e[1];}return total;}
    static int kruskal(int[][] edges,int n){Arrays.sort(edges,Comparator.comparingInt(e->e[2]));int[]p=new int[n];for(int i=0;i<n;i++)p[i]=i;int total=0,c=0;for(int[]e:edges){int a=find(p,e[0]),b=find(p,e[1]);if(a!=b){p[a]=b;total+=e[2];if(++c==n-1)break;}}return total;}static int find(int[]p,int x){return p[x]==x?x:(p[x]=find(p,p[x]));}

    // 10 Dynamic programming
    static int[][] floydWarshall(int[][]d){int n=d.length;int[][]a=new int[n][n];for(int i=0;i<n;i++)a[i]=d[i].clone();for(int k=0;k<n;k++)for(int i=0;i<n;i++)for(int j=0;j<n;j++)if(a[i][k]<1000000&&a[k][j]<1000000)a[i][j]=Math.min(a[i][j],a[i][k]+a[k][j]);return a;}
    static int lcs(String a,String b){int[][]d=new int[a.length()+1][b.length()+1];for(int i=1;i<=a.length();i++)for(int j=1;j<=b.length();j++)d[i][j]=a.charAt(i-1)==b.charAt(j-1)?d[i-1][j-1]+1:Math.max(d[i-1][j],d[i][j-1]);return d[a.length()][b.length()];}

    // 11 Hashing
    static int hashBucket(String key,int size){return Math.floorMod(key.hashCode(),size);}
    static String hashLookup(Map<String,Product>map,String key){Product p=map.get(key.toLowerCase());return p==null?null:p.name;}

    // 12 Greedy
    static int activitySelection(int[][]acts){Arrays.sort(acts,Comparator.comparingInt(a->a[1]));int count=0,last=-1;for(int[]a:acts)if(a[0]>=last){count++;last=a[1];}return count;}
    static double fractionalKnapsack(int budget,List<Product>a){List<Product> copy=new ArrayList<>(a);copy.sort((x,y)->Double.compare(y.rating/Math.max(1,y.minPrice()),x.rating/Math.max(1,x.minPrice())));double value=0,rem=budget;for(Product p:copy){if(rem<=0)break;double take=Math.min(rem,p.minPrice());value+=take*(p.rating/5.0);rem-=take;}return value;}
}
