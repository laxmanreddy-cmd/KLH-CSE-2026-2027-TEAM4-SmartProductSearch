let allProducts=[], selected=new Map();
const $=id=>document.getElementById(id);
const money=n=>"₹"+Number(n).toLocaleString("en-IN");
async function api(url){const r=await fetch(url);if(!r.ok)throw new Error("API error");return r.json();}
async function init(){
  allProducts=await api("/api/products");
  const cats=await api("/api/categories"); cats.forEach(c=>{let o=document.createElement("option");o.textContent=c;$("category").appendChild(o)});
  renderAlgorithms(); await loadProducts(); renderCompare();
}
async function loadProducts(){
  const q=encodeURIComponent($("search").value.trim()), c=encodeURIComponent($("category").value), s=$("sort").value;
  const data=await api(`/api/search?q=${q}&category=${c}&sort=${s}`);
  $("count").textContent=`${data.length} products`;
  $("cards").innerHTML=data.map(card).join("");
}
function card(p){
  const platforms=p.prices.map(x=>`<span class="chip">${x.platform}</span>`).join("");
  const first=p.prices.find(x=>x.price===p.minPrice)||p.prices[0];
  const checked=selected.has(p.id);
  return `<article class="card"><div><span class="tag">${p.category}</span><h3>${p.name}</h3><p class="desc">${p.description}</p><div class="price">${money(p.minPrice)}</div><div class="best">Lowest reference price • ${first.platform}</div><div class="platforms">${platforms}</div></div><div class="card-actions"><button onclick="toggle('${p.id}')">${checked?"✓ Selected":"Compare"}</button><a href="${first.url}" target="_blank" rel="noopener">Open listing</a></div></article>`;
}
function toggle(id){const p=allProducts.find(x=>x.id===id);if(!p)return;if(selected.has(id))selected.delete(id);else if(selected.size<4)selected.set(id,p);else alert("Select up to 4 products.");renderCompare();loadProducts();}
function clearCompare(){selected.clear();renderCompare();loadProducts();}
function renderCompare(){
  const ps=[...selected.values()]; if(!ps.length){$("compareArea").className="compare-empty";$("compareArea").innerHTML="No products selected yet.";return;}
  $("compareArea").className=""; let html=`<table class="compare-table"><thead><tr><th>Product</th><th>Lowest</th><th>Amazon</th><th>Flipkart</th><th>Blinkit</th><th>Zepto</th><th>Myntra</th><th>Meesho</th><th>Tata Neu</th></tr></thead><tbody>`;
  for(const p of ps){let m={};p.prices.forEach(x=>m[x.platform]=x);html+=`<tr><td><b>${p.name}</b><br><span class="muted">${p.category}</span></td><td class="best-cell">${money(p.minPrice)}</td>${["Amazon","Flipkart","Blinkit","Zepto","Myntra","Meesho","Tata Neu"].map(k=>m[k]?`<td>${money(m[k].price)}<br><a href="${m[k].url}" target="_blank">view</a></td>`:`<td>—<br><a href="${p.links[k]}" target="_blank">search</a></td>`).join("")}</tr>`}
  html+="</tbody></table>";$("compareArea").innerHTML=html;
}
async function renderAlgorithms(){
 const groups=[
  ["01 / Searching",["KMP","Linear Search"]],
  ["02 / Basic Sorting",["Bubble Sort","Selection Sort"]],
  ["03 / Advanced Sorting",["Insertion Sort","Merge Sort"]],
  ["04 / Divide & Conquer",["Quick Sort","Heap Sort"]],
  ["05 / Non-Comparison",["Counting Sort","Radix Sort"]],
  ["06 / Trees",["BST Search","AVL Tree"]],
  ["07 / Graph Traversal",["BFS","DFS"]],
  ["08 / Shortest Path",["Dijkstra","Bellman-Ford"]],
  ["09 / MST",["Prim","Kruskal"]],
  ["10 / Dynamic Programming",["Floyd-Warshall","LCS"]],
  ["11 / Hashing",["Separate Chaining","HashMap"]],
  ["12 / Greedy",["Activity Selection","Fractional Knapsack"]]
 ];
 $("algoGrid").innerHTML=groups.map(g=>`<div class="algo-group"><h3>${g[0]}</h3>${g[1].map(a=>`<span>${a}</span>`).join("")}</div>`).join("");
 try{const d=await api("/api/algorithm-status");$("live").innerHTML=`<b>Live Java engine checks</b><br>KMP “iphone” match: ${d.live.kmpIphone} • Linear Search index: ${d.live.linearIndex} • LCS sample: ${d.live.lcs} • Hash bucket: ${d.live.hashBucket}<br>BFS: ${d.live.bfs.join(" → ")}<br>DFS: ${d.live.dfs.join(" → ")}<br>Dijkstra from Amazon: ${Object.entries(d.live.dijkstraAmazon).map(x=>x[0]+":"+x[1]).join(", ")}<br>Bellman-Ford from Amazon: ${Object.entries(d.live.bellmanFordAmazon).map(x=>x[0]+":"+x[1]).join(", ")}<br>Activity-selection count: ${d.live.activitySelection} • Fractional-knapsack score: ${d.live.fractionalKnapsackScore}`;}catch(e){$("live").textContent="Algorithm status will appear after the Java server starts."}
}
init().catch(e=>{console.error(e);$("cards").innerHTML="<div class='compare-empty'>Start with <b>npm run dev</b> and open http://localhost:8080</div>"});
