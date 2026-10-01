// ================================================================
// SMART PRODUCT COMPARATOR — app.js
// Existing + Advanced Algorithm Modules (CO1–CO6)
// ================================================================

let allProducts = [];
let selected = new Map();
let searchRequest = 0;
const $ = id => document.getElementById(id);
const money = value => "₹" + Number(value).toLocaleString("en-IN");
const esc = value => String(value ?? "").replace(/[&<>"']/g, char => ({"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"}[char]));

const categoryInfo = {
  Mobiles: ["📱", "Mobiles"], Laptops: ["💻", "Laptops"], Headphones: ["🎧", "Audio"],
  Shoes: ["👟", "Shoes"], Fashion: ["👕", "Fashion"], Groceries: ["🛒", "Groceries"],
  "Personal Care": ["🧴", "Beauty"], Home: ["🏠", "Home"], Electronics: ["⌨️", "Tech"]
};
const imageFor = category => ({
  Mobiles: "photo-1511707171634-5f897ff02aa9", Laptops: "photo-1496181133206-80ce9b88a853",
  Headphones: "photo-1505740420928-5e560c06d30e", Shoes: "photo-1542291026-7eec264c27ff",
  Fashion: "photo-1521572163474-6864f9cf17ab", Groceries: "photo-1542838132-92c53300491e",
  "Personal Care": "photo-1608248543803-ba4f8c70ae0b", Home: "photo-1555041469-a586c61ea9bc",
  Electronics: "photo-1505740420928-5e560c06d30e"
}[category] || "photo-1490312278390-ab64016e0aa9");
const imageUrl = category => `https://images.unsplash.com/${imageFor(category)}?auto=format&fit=crop&w=700&h=550&q=80`;

async function api(url) {
  const response = await fetch(url);
  if (!response.ok) throw new Error("Request failed: " + response.status);
  return response.json();
}

// ================================================================
// INIT (existing)
// ================================================================
async function init() {
  allProducts = await api("/api/products");
  $("catalogTotal").textContent = allProducts.length;
  const categories = await api("/api/categories");
  const select = $("category");
  categories.forEach(category => {
    const option = document.createElement("option");
    option.value = category; option.textContent = category; select.appendChild(option);
  });
  const makeCategory = (category, tile = false) => {
    const [icon, label] = categoryInfo[category] || ["✦", category];
    if (tile) return `<button class="category-tile" onclick="setCategory('${esc(category)}',this)"><span class="category-icon">${icon}</span><span>${esc(label)}</span><b>→</b></button>`;
    return `<button class="filter-category" data-category="${esc(category)}" onclick="setCategory('${esc(category)}',this)"><span>${icon} &nbsp;${esc(category)}</span><span class="filter-total">${allProducts.filter(item => item.category === category).length}</span></button>`;
  };
  $("categoryTiles").innerHTML = categories.map(category => makeCategory(category, true)).join("");
  $("categoryFilters").innerHTML = categories.map(category => makeCategory(category)).join("");
  $("allCategoryCount").textContent = allProducts.length;
  await loadProducts();
  renderCompare();
  // Auto-load advisor
  selectAdvisor('search', document.querySelector('.advisor-card'));
}

function setCategory(category, button) {
  $("category").value = category;
  $("search").value = "";
  $("catalogSearch").value = "";
  document.querySelectorAll(".filter-category").forEach(item => item.classList.toggle("active", item.dataset.category === category));
  loadProducts();
  $("products").scrollIntoView({ behavior: "smooth", block: "start" });
}

function syncSearch(value) {
  $("search").value = value;
  $("catalogSearch").value = value;
  loadProducts();
}

async function loadProducts() {
  if (!$("search") || !$("category")) return;
  const request = ++searchRequest;
  const query = encodeURIComponent($("search").value.trim());
  const category = encodeURIComponent($("category").value);
  const sort = encodeURIComponent($("sort").value);
  try {
    const products = await api(`/api/search?q=${query}&category=${category}&sort=${sort}`);
    if (request !== searchRequest) return;
    $("count").textContent = `${products.length} ${products.length === 1 ? "product" : "products"}`;
    $("cards").innerHTML = products.map(card).join("") || noResults();
  } catch (error) {
    if (request !== searchRequest) return;
    const needle = $("search").value.trim().toLocaleLowerCase();
    const chosenCategory = $("category").value;
    let products = allProducts.filter(product =>
      (chosenCategory === "All" || product.category.toLocaleLowerCase() === chosenCategory.toLocaleLowerCase()) &&
      (!needle || `${product.name} ${product.category} ${product.description}`.toLocaleLowerCase().includes(needle))
    );
    if ($("sort").value === "price") products.sort((a, b) => a.minPrice - b.minPrice);
    else if ($("sort").value === "name") products.sort((a, b) => a.name.localeCompare(b.name));
    else if ($("sort").value === "rating") products.sort((a, b) => b.rating - a.rating);
    $("count").textContent = `${products.length} ${products.length === 1 ? "product" : "products"}`;
    $("cards").innerHTML = products.map(card).join("") || noResults();
  }
}

function noResults() {
  return `<div class="no-results"><span>⌕</span><b>No matching products yet</b><p>Try another search or choose a different category.</p><button onclick="syncSearch('')">Show all products</button></div>`;
}

// ================================================================
// PRODUCT CARD (existing)
// ================================================================
function card(product) {
  const prices = product.prices || [];
  const lowest = prices.find(item => item.price === product.minPrice) || prices[0];
  const shownPrice = product.minPrice < 2147483647 ? money(product.minPrice) : "Check store";
  const platforms = [...new Set(prices.map(item => item.platform))].slice(0, 3);
  const platformLogos = platforms.map(name => `<span class="store-logo ${esc(name.toLowerCase().replace(/\s/g, "-"))}" title="${esc(name)}">${name === "Amazon" ? "a" : name === "Flipkart" ? "f" : name === "Myntra" ? "M" : name === "Meesho" ? "m" : name === "Blinkit" ? "b" : name === "Zepto" ? "z" : "T"}</span>`).join("");
  const isSelected = selected.has(product.id);
  return `<article class="product-card"><div class="product-image-wrap"><img class="product-image" src="${imageUrl(product.category)}" alt="${esc(product.name)}" loading="lazy"><span class="image-category">${esc(product.category)}</span><button class="save-product ${isSelected ? "saved" : ""}" onclick="toggle('${esc(product.id)}')" aria-label="${isSelected ? "Remove from" : "Add to"} compare">${isSelected ? "♥" : "♡"}</button></div><div class="product-info"><div class="product-title-row"><h3>${esc(product.name)}</h3><span class="rating">★ ${Number(product.rating).toFixed(1)}</span></div><p class="product-desc">${esc(product.description)}</p><div class="product-price-row"><div><span class="price-label">From</span><strong class="price">${shownPrice}</strong></div><span class="best-tag">${lowest ? "Best listed price" : "Store listings"}</span></div><div class="store-row">${platformLogos}<span>${prices.length} store listings</span></div><div class="card-actions"><button class="compare-btn ${isSelected ? "selected" : ""}" onclick="toggle('${esc(product.id)}')">${isSelected ? "✓ Added to compare" : "+ Add to compare"}</button><a class="shop-btn" href="#" onclick="openStore(event, '${esc(product.id)}')">View deal <span>→</span></a></div></div></article>`;
}

function openStore(event, id) {
  event.preventDefault();
  const product = allProducts.find(item => item.id === id);
  const cheapest = (product?.prices || []).find(item => item.price === product.minPrice) || product?.prices?.[0];
  if (cheapest?.url) window.open(cheapest.url, "_blank", "noopener,noreferrer");
  else if (product?.links?.Amazon) window.open(product.links.Amazon, "_blank", "noopener,noreferrer");
}

function toggle(id) {
  const product = allProducts.find(item => item.id === id);
  if (!product) return;
  if (selected.has(id)) selected.delete(id);
  else if (selected.size < 4) selected.set(id, product);
  else { alert("You can compare up to four products. Remove one to add another."); return; }
  $("savedCount").textContent = selected.size;
  renderCompare();
  loadProducts();
}

function clearCompare() {
  selected.clear(); $("savedCount").textContent = "0"; renderCompare(); loadProducts();
}

function renderCompare() {
  const products = [...selected.values()];
  if (!products.length) {
    $("compareArea").className = "compare-empty";
    $("compareArea").innerHTML = `<span class="compare-empty-icon">⚖️</span><b>Your shortlist starts here</b><span>Choose "Add to compare" on a product card to see your picks side by side.</span><a href="#products">Explore popular picks <span>→</span></a>`;
    return;
  }
  const platforms = [...new Set(products.flatMap(product => (product.prices || []).map(item => item.platform)))];
  let html = `<table class="compare-table"><thead><tr><th>Product</th><th>Best listed</th>${platforms.map(name => `<th>${esc(name)}</th>`).join("")}<th></th></tr></thead><tbody>`;
  for (const product of products) {
    const byPlatform = Object.fromEntries((product.prices || []).map(item => [item.platform, item]));
    html += `<tr><td><b>${esc(product.name)}</b><br><span class="muted">${esc(product.category)} · ★ ${Number(product.rating).toFixed(1)}</span></td><td class="best-cell">${product.minPrice < 2147483647 ? money(product.minPrice) : "Check store"}</td>`;
    html += platforms.map(name => byPlatform[name] ? `<td>${money(byPlatform[name].price)}<br><a href="${esc(byPlatform[name].url || "#")}" target="_blank" rel="noopener noreferrer">View store ↗</a></td>` : `<td class="unavailable">—</td>`).join("");
    html += `<td><button class="remove-compare" onclick="toggle('${esc(product.id)}')" aria-label="Remove ${esc(product.name)}">×</button></td></tr>`;
  }
  $("compareArea").className = "compare-table-wrap";
  $("compareArea").innerHTML = html + "</tbody></table>";
}

// ================================================================
// MODULE SWITCHER
// ================================================================
function showModule(name) {
  document.querySelectorAll(".algo-module").forEach(el => el.classList.remove("active-module"));
  document.querySelectorAll(".algo-tab").forEach(el => el.classList.remove("active"));
  const mod = $(`module-${name}`);
  if (mod) mod.classList.add("active-module");
  const tab = $(`tab-${name}`);
  if (tab) tab.classList.add("active");
  $("algo-modules").scrollIntoView({ behavior: "smooth", block: "start" });
}

// ================================================================
// CO1 — ALGORITHM ADVISOR
// ================================================================
async function selectAdvisor(problem, btn) {
  document.querySelectorAll(".advisor-card").forEach(c => c.classList.remove("selected"));
  if (btn) btn.classList.add("selected");
  $("advisor-result").innerHTML = `<div class="algo-loading">Loading recommendations…</div>`;
  try {
    const data = await api(`/api/advisor?problem=${problem}`);
    renderAdvisor(data);
  } catch (e) {
    $("advisor-result").innerHTML = `<div class="algo-error">Backend not running. Start the server first.</div>`;
  }
}

function renderAdvisor(data) {
  if (data.error) { $("advisor-result").innerHTML = `<div class="algo-error">${esc(data.error)}</div>`; return; }
  let html = `<div class="advisor-output">
    <div class="advisor-problem"><span class="advisor-class-badge">${esc(data.class)}</span><h4>${esc(data.problem)}</h4></div>
    <div class="algo-cards-row">`;
  for (const a of (data.algorithms || [])) {
    html += `<div class="algo-rec-card">
      <div class="algo-rec-name">${esc(a.name)}</div>
      <div class="algo-rec-why">${esc(a.why)}</div>
      <div class="algo-rec-complexity"><span>Time: <b>${esc(a.time)}</b></span> <span>Space: <b>${esc(a.space)}</b></span></div>
    </div>`;
  }
  html += `</div></div>`;
  $("advisor-result").innerHTML = html;
}

// ================================================================
// CO2 — ADVANCED SEARCH
// ================================================================
function clearSearchResults() {
  $("search-results").innerHTML = "";
  $("search-products").innerHTML = "";
  $("search-explanation").innerHTML = "";
}

async function runAdvancedSearch() {
  const query = $("adv-query").value.trim();
  const algo = document.querySelector('input[name="search-algo"]:checked')?.value || "kmp";
  if (!query) { $("search-results").innerHTML = `<div class="algo-error">Please enter a search term.</div>`; return; }
  $("search-results").innerHTML = `<div class="algo-loading">Running ${algo.toUpperCase()} search…</div>`;
  $("search-products").innerHTML = "";
  $("search-explanation").innerHTML = "";
  try {
    const data = await api(`/api/advanced-search?q=${encodeURIComponent(query)}&algo=${algo}`);
    renderSearchResult(data, [data]);
    showSearchExplanation(algo, query, data);
  } catch (e) {
    $("search-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

async function runAllSearchAlgos() {
  const query = $("adv-query").value.trim();
  if (!query) { $("search-results").innerHTML = `<div class="algo-error">Please enter a search term.</div>`; return; }
  $("search-results").innerHTML = `<div class="algo-loading">Running all 4 algorithms…</div>`;
  $("search-products").innerHTML = "";
  const algos = ["kmp", "z", "rabin", "suffix"];
  try {
    const results = await Promise.all(algos.map(a => api(`/api/advanced-search?q=${encodeURIComponent(query)}&algo=${a}`)));
    renderSearchComparison(results);
  } catch (e) {
    $("search-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

function renderSearchResult(data, all) {
  let html = `<div class="result-metric-row">
    <div class="metric-card"><div class="metric-val">${data.matches}</div><div class="metric-label">Products Matched</div></div>
    <div class="metric-card"><div class="metric-val">${data.totalOccurrences}</div><div class="metric-label">Total Occurrences</div></div>
    <div class="metric-card highlight"><div class="metric-val">${Number(data.timeMs).toFixed(3)} ms</div><div class="metric-label">Execution Time</div></div>
    <div class="metric-card"><div class="metric-val">${data.corpusSize}</div><div class="metric-label">Documents Searched</div></div>
  </div>`;
  $("search-results").innerHTML = html;
  if (data.products && data.products.length > 0) {
    $("search-products").innerHTML = data.products.map(card).join("");
  }
}

function renderSearchComparison(results) {
  const algoNames = {"kmp": "KMP", "z": "Z-Function", "rabin": "Rabin-Karp", "suffix": "Suffix Array"};
  let html = `<div class="comparison-table-wrap"><table class="compare-table"><thead><tr><th>Algorithm</th><th>Matches</th><th>Occurrences</th><th>Time (ms)</th><th>Complexity</th></tr></thead><tbody>`;
  const complexities = {kmp:"O(n+m)", z:"O(n+m)", rabin:"O(n+m) avg", suffix:"O(m log n)"};
  let fastest = results.reduce((a, b) => parseFloat(a.timeMs) < parseFloat(b.timeMs) ? a : b);
  for (const r of results) {
    const isFastest = r.algorithm === fastest.algorithm;
    html += `<tr class="${isFastest ? "best-row" : ""}"><td><b>${esc(algoNames[r.algorithm] || r.algorithm)}</b>${isFastest ? ' <span class="best-tag">Fastest</span>' : ""}</td><td>${r.matches}</td><td>${r.totalOccurrences}</td><td class="${isFastest ? "best-cell" : ""}">${Number(r.timeMs).toFixed(4)}</td><td>${esc(complexities[r.algorithm] || "")}</td></tr>`;
  }
  html += `</tbody></table></div>`;
  $("search-results").innerHTML = html;
  if (results[0].products && results[0].products.length > 0) {
    $("search-products").innerHTML = results[0].products.map(card).join("");
  }
}

function showSearchExplanation(algo, pattern, data) {
  const explanations = {
    kmp: `<b>KMP — Knuth-Morris-Pratt:</b> Builds a failure (LPS) array from the pattern. On mismatch, uses LPS to skip without re-comparing already matched characters. Never backtracks on the text.<br><b>LPS Array for "${esc(pattern)}":</b> ${data.lps || "computed on backend"}`,
    z: `<b>Z-Function:</b> z[i] = length of longest substring starting at i that is also a prefix of the string. Pattern is prepended with '$' separator. Match at position i iff z[i] == pattern.length.`,
    rabin: `<b>Rabin-Karp:</b> Computes rolling polynomial hash. Pattern hash is pre-computed. Slide a window over text updating hash in O(1). Verify on hash match (eliminates false positives).`,
    suffix: `<b>Suffix Array:</b> All suffixes sorted lexicographically. Binary search locates all occurrences of pattern in O(m log n) after O(n log n) construction. LCP array enables efficient repeated queries.`
  };
  $("search-explanation").innerHTML = `<div class="explain-box"><div class="explain-icon">💡</div><div>${explanations[algo] || ""}</div></div>`;
}

// ================================================================
// CO3 — BUNDLE OPTIMIZER
// ================================================================
async function runBundleOptimizer() {
  const budget = $("bundle-budget").value;
  const categories = $("bundle-categories").value.trim();
  const algo = $("bundle-algo").value;
  $("bundle-results").innerHTML = `<div class="algo-loading">Running ${algo} DP…</div>`;
  $("bundle-products").innerHTML = "";
  try {
    const data = await api(`/api/bundle?budget=${budget}&algo=${algo}&categories=${encodeURIComponent(categories)}`);
    renderBundleResult(data);
  } catch (e) {
    $("bundle-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

function renderBundleResult(data) {
  const algoNames = {bitmask:"Bitmask DP", subset:"Subset DP (0-1 Knapsack)", interval:"Interval DP", tree:"Tree DP"};
  let html = `<div class="result-metric-row">
    <div class="metric-card highlight"><div class="metric-val">${algoNames[data.algorithm] || data.algorithm}</div><div class="metric-label">Algorithm Used</div></div>
    <div class="metric-card"><div class="metric-val">${money(data.totalCost)}</div><div class="metric-label">Total Cost</div></div>
    <div class="metric-card"><div class="metric-val">${money(data.budgetRemaining)}</div><div class="metric-label">Budget Remaining</div></div>
    <div class="metric-card"><div class="metric-val">${Number(data.timeMs).toFixed(3)} ms</div><div class="metric-label">Execution Time</div></div>
  </div>
  <div class="explain-box" style="margin-top:12px">
    <div class="explain-icon">🧮</div>
    <div><b>${esc(data.complexity)}</b> — ${esc(data.description)}</div>
  </div>`;
  $("bundle-results").innerHTML = html;
  if (data.selected && data.selected.length > 0) {
    $("bundle-products").innerHTML = data.selected.map(card).join("");
  } else {
    $("bundle-products").innerHTML = `<div class="no-results"><span>📦</span><b>No products fit the budget with this configuration.</b><p>Try increasing the budget or changing categories.</p></div>`;
  }
}

// ================================================================
// CO4 — NETWORK FLOW
// ================================================================
async function runFlowAlgo() {
  const algo = $("flow-algo").value;
  $("flow-graph-container").innerHTML = `<div class="algo-loading">Running ${algo}…</div>`;
  $("flow-results").innerHTML = "";
  try {
    const data = await api(`/api/flow?algo=${algo}`);
    renderFlowResult(data);
  } catch (e) {
    $("flow-graph-container").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

async function runAllFlowAlgos() {
  $("flow-results").innerHTML = `<div class="algo-loading">Running all 3 flow algorithms…</div>`;
  $("flow-graph-container").innerHTML = "";
  try {
    const algos = ["ford", "edmonds", "dinic"];
    const results = await Promise.all(algos.map(a => api(`/api/flow?algo=${a}`)));
    renderFlowComparison(results);
    renderFlowGraph(results[2]); // show Dinic graph
  } catch (e) {
    $("flow-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

function renderFlowResult(data) {
  renderFlowGraph(data);
  const algoNames = {ford:"Ford-Fulkerson", edmonds:"Edmonds-Karp", dinic:"Dinic"};
  let html = `<div class="result-metric-row" style="margin-top:14px">
    <div class="metric-card highlight"><div class="metric-val">${data.maxFlow}</div><div class="metric-label">Maximum Flow</div></div>
    <div class="metric-card"><div class="metric-val">${(data.minCut || []).length}</div><div class="metric-label">Min-Cut Edges</div></div>
    <div class="metric-card"><div class="metric-val">${Number(data.timeMs).toFixed(4)} ms</div><div class="metric-label">Execution Time</div></div>
  </div>`;
  $("flow-results").innerHTML = html;
}

function renderFlowComparison(results) {
  const algoNames = {ford:"Ford-Fulkerson", edmonds:"Edmonds-Karp", dinic:"Dinic"};
  const complexities = {ford:"O(E × f)", edmonds:"O(VE²)", dinic:"O(V²E)"};
  let html = `<div class="comparison-table-wrap"><table class="compare-table"><thead><tr><th>Algorithm</th><th>Max Flow</th><th>Time (ms)</th><th>Complexity</th></tr></thead><tbody>`;
  let fastest = results.reduce((a, b) => parseFloat(a.timeMs) < parseFloat(b.timeMs) ? a : b);
  for (const r of results) {
    const isFastest = r.algorithm === fastest.algorithm;
    html += `<tr class="${isFastest ? "best-row" : ""}"><td><b>${esc(algoNames[r.algorithm] || r.algorithm)}</b>${isFastest ? ' <span class="best-tag">Fastest</span>' : ""}</td><td>${r.maxFlow}</td><td class="${isFastest ? "best-cell" : ""}">${Number(r.timeMs).toFixed(4)}</td><td>${esc(complexities[r.algorithm] || "")}</td></tr>`;
  }
  html += `</tbody></table></div>`;
  $("flow-results").innerHTML = html;
}

function renderFlowGraph(data) {
  if (!data || !data.nodeLabels) return;
  const labels = data.nodeLabels;
  const n = labels.length;
  const P = data.products ? data.products.length : 0;
  const PL = data.platforms ? data.platforms.length : 0;

  // Simple SVG flow graph
  const W = 680, H = 320;
  // Layout: source left, products middle-left, platforms middle-right, sink right
  function nodeX(i) {
    if (i === 0) return 60;
    if (i === n - 1) return W - 60;
    if (i <= P) return W * 0.35;
    return W * 0.65;
  }
  function nodeY(i) {
    if (i === 0 || i === n - 1) return H / 2;
    if (i <= P) return 30 + ((i - 1) / Math.max(P - 1, 1)) * (H - 60);
    const j = i - P - 1;
    return 60 + (j / Math.max(PL - 1, 1)) * (H - 120);
  }

  let svg = `<svg viewBox="0 0 ${W} ${H}" xmlns="http://www.w3.org/2000/svg" class="flow-svg">
    <defs>
      <marker id="arrowhead" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
        <polygon points="0 0, 8 3, 0 6" fill="#5549dd"/>
      </marker>
      <marker id="arrowcut" markerWidth="8" markerHeight="6" refX="8" refY="3" orient="auto">
        <polygon points="0 0, 8 3, 0 6" fill="#e05350"/>
      </marker>
    </defs>`;

  const cutEdges = new Set((data.minCut || []).map(e => `${e.from}-${e.to}`));
  for (const edge of (data.edges || [])) {
    const x1 = nodeX(edge.from), y1 = nodeY(edge.from);
    const x2 = nodeX(edge.to),   y2 = nodeY(edge.to);
    const isCut = cutEdges.has(`${edge.from}-${edge.to}`);
    const isUsed = edge.used > 0;
    const color = isCut ? "#e05350" : isUsed ? "#5549dd" : "#c5c9d8";
    const width = isCut ? 2.5 : isUsed ? 2 : 1;
    const marker = isCut ? "url(#arrowcut)" : "url(#arrowhead)";
    svg += `<line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${color}" stroke-width="${width}" marker-end="${marker}" opacity="${isCut ? 1 : isUsed ? 0.9 : 0.4}"/>`;
    const mx = (x1 + x2) / 2, my = (y1 + y2) / 2;
    svg += `<text x="${mx}" y="${my - 4}" font-size="9" fill="${color}" text-anchor="middle">${edge.used}/${edge.capacity}</text>`;
  }
  for (let i = 0; i < n; i++) {
    const x = nodeX(i), y = nodeY(i);
    const isSource = i === 0, isSink = i === n - 1;
    const fill = isSource ? "#34845a" : isSink ? "#5549dd" : i <= P ? "#6b7cf5" : "#f0a830";
    svg += `<circle cx="${x}" cy="${y}" r="22" fill="${fill}" stroke="white" stroke-width="2"/>`;
    const shortLabel = labels[i] ? labels[i].substring(0, 8) : "";
    svg += `<text x="${x}" y="${y + 4}" font-size="9" fill="white" text-anchor="middle" font-weight="700">${esc(shortLabel)}</text>`;
  }
  svg += `</svg>
    <div class="flow-legend">
      <span><span class="legend-dot" style="background:#5549dd"></span> Flow edge (used)</span>
      <span><span class="legend-dot" style="background:#e05350"></span> Min-cut edge</span>
      <span><span class="legend-dot" style="background:#c5c9d8"></span> Unused capacity</span>
    </div>`;
  $("flow-graph-container").innerHTML = svg;
}

// ================================================================
// CO5 — NP LAB
// ================================================================
async function runNpLab() {
  const budget = $("np-budget").value;
  const n = $("np-n").value;
  $("np-results").innerHTML = `<div class="algo-loading">Running exact O(2ⁿ) and greedy approximation…</div>`;
  try {
    const data = await api(`/api/np-optimize?budget=${budget}&n=${n}`);
    renderNpResult(data);
  } catch (e) {
    $("np-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

function renderNpResult(data) {
  const ratio = data.approximationRatio;
  const ratioColor = ratio >= 0.9 ? "#34845a" : ratio >= 0.7 ? "#d69138" : "#e05350";
  let html = `
  <div class="np-compare-grid">
    <div class="np-solution-card exact-card">
      <div class="np-sol-label">EXACT (Brute Force)</div>
      <div class="np-sol-complexity">${esc(data.exact.complexity)}</div>
      <div class="np-sol-time">${Number(data.exact.timeMs).toFixed(4)} ms</div>
      <div class="np-sol-score">Score: <b>${data.exact.score}</b></div>
      <div class="np-sol-cost">Cost: ${money(data.exact.cost)}</div>
      <div class="np-sol-products">${(data.exact.products || []).map(p => `<span class="prod-chip">${esc(p)}</span>`).join("")}</div>
    </div>
    <div class="np-approx-arrow">≈<br><small style="font-size:10px">ratio</small><br><b style="color:${ratioColor}">${Number(ratio * 100).toFixed(1)}%</b></div>
    <div class="np-solution-card approx-card">
      <div class="np-sol-label">APPROX (Greedy Ratio)</div>
      <div class="np-sol-complexity">${esc(data.approx.complexity)}</div>
      <div class="np-sol-time">${Number(data.approx.timeMs).toFixed(4)} ms</div>
      <div class="np-sol-score">Score: <b>${data.approx.score}</b></div>
      <div class="np-sol-cost">Cost: ${money(data.approx.cost)}</div>
      <div class="np-sol-products">${(data.approx.products || []).map(p => `<span class="prod-chip approx-chip">${esc(p)}</span>`).join("")}</div>
    </div>
  </div>
  <div class="explain-box" style="margin-top:14px">
    <div class="explain-icon">📐</div>
    <div>Approximation ratio = ${Number(ratio * 100).toFixed(1)}% of optimal. As n increases, exact search grows as 2ⁿ while greedy remains O(n log n). This is why NP-hard problems require approximation algorithms for large inputs.</div>
  </div>`;
  $("np-results").innerHTML = html;
}

// ================================================================
// CO6 — RANDOMIZED
// ================================================================
async function runRandomized() {
  const k = $("lv-k").value;
  const num = $("mc-num").value;
  const witnesses = $("mc-witnesses").value;
  const seed = $("rand-seed").value;
  $("random-results").innerHTML = `<div class="algo-loading">Running randomized algorithms…</div>`;
  try {
    const data = await api(`/api/randomized?algo=both&k=${k}&num=${num}&witnesses=${witnesses}&seed=${seed}`);
    renderRandomizedResult(data);
  } catch (e) {
    $("random-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

function renderRandomizedResult(data) {
  let html = "";
  if (data.lasVegas) {
    const lv = data.lasVegas;
    html += `<div class="rand-card lv-card">
      <div class="rand-badge">Las Vegas</div>
      <div class="rand-title">${esc(lv.algorithm)}</div>
      <div class="rand-desc">${esc(lv.description)}</div>
      <div class="result-metric-row" style="margin-top:10px">
        <div class="metric-card"><div class="metric-val">${money(lv.result)}</div><div class="metric-label">${lv.k}-th smallest price</div></div>
        <div class="metric-card"><div class="metric-val">${lv.inputSize}</div><div class="metric-label">Input Size</div></div>
        <div class="metric-card highlight"><div class="metric-val">${Number(lv.timeMs).toFixed(4)} ms</div><div class="metric-label">Execution Time</div></div>
        <div class="metric-card"><div class="metric-val">Seed: ${lv.seed}</div><div class="metric-label">Random Seed</div></div>
      </div>
      <div class="rand-property">✅ Always correct — this is the Las Vegas guarantee.</div>
    </div>`;
  }
  if (data.monteCarlo) {
    const mc = data.monteCarlo;
    html += `<div class="rand-card mc-card" style="margin-top:14px">
      <div class="rand-badge mc">Monte Carlo</div>
      <div class="rand-title">${esc(mc.algorithm)}</div>
      <div class="rand-desc">${esc(mc.description)}</div>
      <div class="result-metric-row" style="margin-top:10px">
        <div class="metric-card"><div class="metric-val">${mc.testNumber}</div><div class="metric-label">Test Number</div></div>
        <div class="metric-card highlight"><div class="metric-val">${mc.probablyPrime ? "✅ Probably Prime" : "❌ Composite"}</div><div class="metric-label">Result</div></div>
        <div class="metric-card"><div class="metric-val">${mc.witnesses} witnesses</div><div class="metric-label">Confidence</div></div>
        <div class="metric-card"><div class="metric-val">${Number(mc.timeMs).toFixed(4)} ms</div><div class="metric-label">Execution Time</div></div>
      </div>
      <div class="rand-property">⚠️ Correct with probability ≥ 1 − 4^(−${mc.witnesses}). Not 100% guaranteed — this is the Monte Carlo nature.</div>
      ${mc.priceTests ? `<div style="margin-top:10px"><b>Primality test on product prices:</b><div class="price-test-row">${(mc.priceTests||[]).map(t=>`<span class="price-test-chip ${t.probablyPrime?"prime":"composite"}">${money(t.number)} ${t.probablyPrime?"✓ Prime":"× Composite"}</span>`).join("")}</div></div>` : ""}
    </div>`;
  }
  $("random-results").innerHTML = html;
}

// ================================================================
// CO6 — PARALLEL
// ================================================================
async function runParallel() {
  $("parallel-results").innerHTML = `<div class="algo-loading">Running parallel algorithms…</div>`;
  $("workspan-viz").innerHTML = "";
  try {
    const data = await api("/api/parallel?algo=both");
    renderParallelResult(data);
  } catch (e) {
    $("parallel-results").innerHTML = `<div class="algo-error">Backend error: ${e.message}</div>`;
  }
}

function renderParallelResult(data) {
  const input = data.input || [];
  let html = `<div class="parallel-input">
    <b>Input (Product Prices):</b>
    <div class="price-chips">${input.map(v => `<span class="price-chip">₹${Number(v).toLocaleString("en-IN")}</span>`).join("")}</div>
  </div>`;

  if (data.prefixSum) {
    const ps = data.prefixSum;
    const faster = ps.parTimeMs < ps.seqTimeMs ? "Parallel" : "Sequential";
    const note = ps.parTimeMs < ps.seqTimeMs ? "Parallel wins — thread pool amortised over dataset." : "Sequential wins for small dataset — thread overhead dominates.";
    html += `<div class="parallel-section">
      <h4>Prefix Sum <span class="complexity-badge">O(n) work · O(log n) span</span></h4>
      <div class="parallel-arrays">
        <div><b>Sequential Result:</b><div class="price-chips">${(ps.sequential||[]).map(v=>`<span class="price-chip">${Number(v).toLocaleString("en-IN")}</span>`).join("")}</div></div>
        <div><b>Parallel Result:</b><div class="price-chips">${(ps.parallel||[]).map(v=>`<span class="price-chip par-chip">${Number(v).toLocaleString("en-IN")}</span>`).join("")}</div></div>
      </div>
      <div class="result-metric-row">
        <div class="metric-card"><div class="metric-val">${Number(ps.seqTimeMs).toFixed(4)} ms</div><div class="metric-label">Sequential Time</div></div>
        <div class="metric-card highlight"><div class="metric-val">${Number(ps.parTimeMs).toFixed(4)} ms</div><div class="metric-label">Parallel Time</div></div>
        <div class="metric-card"><div class="metric-val">${faster} Faster</div><div class="metric-label">Winner</div></div>
      </div>
      <div class="rand-property">${note}</div>
      ${renderWorkSpan("Prefix Sum", ps.work, ps.span, ps.parallelism)}
    </div>`;
  }

  if (data.reduce) {
    const r = data.reduce;
    const faster = r.parTimeMs < r.seqTimeMs ? "Parallel" : "Sequential";
    const note = r.parTimeMs < r.seqTimeMs ? "Parallel reduces faster — tree reduction over multiple cores." : "Sequential faster — thread overhead > reduction cost for small arrays.";
    html += `<div class="parallel-section">
      <h4>Reduce (Sum) <span class="complexity-badge">O(n) work · O(log n) span</span></h4>
      <div class="result-metric-row">
        <div class="metric-card highlight"><div class="metric-val">${money(r.seqResult)}</div><div class="metric-label">Sequential Sum</div></div>
        <div class="metric-card highlight"><div class="metric-val">${money(r.parResult)}</div><div class="metric-label">Parallel Sum</div></div>
        <div class="metric-card"><div class="metric-val">${Number(r.seqTimeMs).toFixed(4)} ms</div><div class="metric-label">Seq Time</div></div>
        <div class="metric-card"><div class="metric-val">${Number(r.parTimeMs).toFixed(4)} ms</div><div class="metric-label">Par Time</div></div>
        <div class="metric-card"><div class="metric-val">${faster} Faster</div><div class="metric-label">Winner</div></div>
      </div>
      <div class="rand-property">${note}</div>
      ${renderWorkSpan("Reduce", r.work, r.span, r.parallelism)}
    </div>`;
  }
  $("parallel-results").innerHTML = html;
}

function renderWorkSpan(name, work, span, parallelism) {
  return `<div class="workspan-card">
    <div class="workspan-title">Work-Span Analysis — ${esc(name)}</div>
    <div class="workspan-metrics">
      <div class="ws-metric"><span class="ws-label">Work (W)</span><span class="ws-val">${work}</span><span class="ws-desc">Total operations</span></div>
      <div class="ws-metric"><span class="ws-label">Span (S)</span><span class="ws-val">${span}</span><span class="ws-desc">Critical path length</span></div>
      <div class="ws-metric highlight"><span class="ws-label">Parallelism</span><span class="ws-val">${Number(parallelism).toFixed(2)}</span><span class="ws-desc">W / S — max speedup</span></div>
    </div>
    <div class="rand-property" style="margin-top:8px">Parallelism = Work / Span ≈ ${Number(parallelism).toFixed(2)}×. Ideal speedup with unlimited processors.</div>
  </div>`;
}

// ================================================================
// ENTRY POINT
// ================================================================
init().catch(error => {
  console.error(error);
  $("cards").innerHTML = "<div class='no-results'>Start the app with <b>npm run dev</b> or <b>java Main</b>, then open http://localhost:8080.</div>";
});
