let allProducts = [];
let selected = new Map();
let searchRequest = 0;
const $ = id => document.getElementById(id);
const money = value => "₹" + Number(value).toLocaleString("en-IN");
const esc = value => String(value ?? "").replace(/[&<>"']/g, char => ({
  "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
}[char]));
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
    $("cards").innerHTML = products.map(card).join("") || `<div class="no-results"><span>⌕</span><b>No matching products yet</b><p>Try another search or choose a different category.</p><button onclick="syncSearch('')">Show all products</button></div>`;
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
    $("cards").innerHTML = products.map(card).join("") || `<div class="no-results"><span>⌕</span><b>No matching products yet</b><p>Try another search or choose a different category.</p><button onclick="syncSearch('')">Show all products</button></div>`;
  }
}

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
    $("compareArea").innerHTML = `<span class="compare-empty-icon">⚖️</span><b>Your shortlist starts here</b><span>Choose “Add to compare” on a product card to see your picks side by side.</span><a href="#products">Explore popular picks <span>→</span></a>`;
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

init().catch(error => {
  console.error(error);
  $("cards").innerHTML = "<div class='no-results'>Start the app with <b>npm run dev</b>, then open http://localhost:8080.</div>";
});
