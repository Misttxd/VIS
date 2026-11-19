const byId = id => document.getElementById(id);
let ingredients = [];
let recipes = [];
let categories = [];
let mealPlans = [];
let chooseFreeDay = true;

async function api(path, body) {
    const options = {};
    if (body !== undefined) {
        options.method = "POST";
        options.headers = {"Content-Type": "application/json"};
        options.body = JSON.stringify(body);
    }
    const response = await fetch("/api/" + path, options);
    const text = await response.text();
    if (!response.ok) {
        throw new Error(text || "Požadavek se nezdařil.");
    }
    if (text) {
        return JSON.parse(text);
    }
}

function listen(id, eventName, action) {
    byId(id).addEventListener(eventName, async event => {
        event.preventDefault();
        try {
            await action();
        } catch (error) {
            byId("message").textContent = error.message;
        }
    });
}

function list(id, rows, emptyMessage) {
    const target = byId(id);
    target.replaceChildren();
    if (rows.length === 0) {
        rows = [emptyMessage];
    }
    for (const text of rows) {
        const item = document.createElement("li");
        item.textContent = text;
        target.append(item);
    }
}

function choices(select, items, label) {
    const previous = select.value;
    select.replaceChildren();
    for (const item of items) {
        select.add(new Option(label(item), item.id));
    }
    if (items.some(item => String(item.id) === previous)) {
        select.value = previous;
    }
}

function ingredientLabel(item) {
    return item.name + " (" + item.unit + ")";
}

function ingredient(id) {
    return ingredients.find(item => item.id === id);
}

function recipe(id) {
    return recipes.find(item => item.id === id);
}

function today() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 10);
}

async function showCatalog() {
    const items = await api("ingredients?storage=" + byId("storage").value);
    const catalog = byId("catalog");
    catalog.replaceChildren();
    if (items.length === 0) {
        catalog.textContent = "V tomto seznamu zatím nejsou žádné suroviny.";
    }
    for (const category of categories) {
        const group = items.filter(item => item.categoryId === category.id);
        if (group.length > 0) {
            const block = document.createElement("div");
            const heading = document.createElement("strong");
            heading.textContent = category.name;
            const names = document.createElement("p");
            names.textContent = group.map(ingredientLabel).join(", ");
            block.append(heading, names);
            catalog.append(block);
        }
    }
}

async function showStock() {
    const overview = await api("stock/overview?day=" + today());
    const low = overview.low;
    byId("lowCard").classList.toggle("attention", low.length > 0);
    list("lowStock", low.map(stock => stock.name + " · zbývá " + stock.available + " " + stock.unit),
        "Žádná z dostupných surovin nedochází.");
    list("levels", overview.available.map(stock => {
        let text = stock.name + " · " + stock.available + " " + stock.unit;
        if (stock.low) {
            text += " · pod minimální zásobou";
        }
        return text;
    }), "Na skladě nejsou žádné dostupné zásoby.");

    function deliveryLabel(delivery) {
        const item = ingredient(delivery.ingredientId);
        return "#" + delivery.id + " " + item.name + " · " + delivery.remaining
            + " " + item.unit + " · expirace " + delivery.expires;
    }
    const deliveries = overview.deliveries;
    choices(byId("withdrawDelivery"), deliveries, deliveryLabel);
    list("deliveries", deliveries.map(deliveryLabel), "Sklad je prázdný.");
    const expiring = await api("expiring?day=" + today());
    byId("expiryCard").classList.toggle("attention", expiring.length > 0);
    list("warnings", expiring.map(deliveryLabel), "V nejbližších třech dnech nic neexpiruje.");
}

async function showRecipe() {
    const id = Number(byId("recipeChoice").value);
    if (!id) {
        byId("instructions").textContent = "";
        list("detail", [], "Zatím žádný recept.");
        return;
    }
    const selected = await api("recipes/" + id);
    byId("instructions").textContent = selected.servings + " porcí · " + selected.instructions;
    list("detail", selected.ingredients.map(item =>
        item.name + " · " + item.quantity + " " + item.unit + " · " + item.category
    ), "Žádné suroviny.");
}

let portionsByRecipe = new Map();

function portionCount(count) {
    if (count === 1) {
        return "1 porce";
    }
    if (count >= 2 && count <= 4) {
        return count + " porce";
    }
    return count + " porcí";
}

function planRecipeLabel(recipe) {
    if (portionsByRecipe.has(recipe.id)) {
        return recipe.name + " · " + portionCount(portionsByRecipe.get(recipe.id));
    }
    return recipe.name;
}

function refreshPlanChoices() {
    for (const select of document.querySelectorAll("#planLines select")) {
        choices(select, recipes, planRecipeLabel);
    }
}

function firstFreeDay() {
    const date = byId("day");
    date.value = today();
    while (mealPlans.some(plan => plan.day === date.value)) {
        date.stepUp();
    }
}

function updatePlanFeedback() {
    const day = byId("day").value;
    let message = "";
    if (!day) {
        message = "Vyber datum plánu.";
    } else if (mealPlans.some(plan => plan.day === day)) {
        message = "Pro tento den už plán existuje. Pro nový plán vyber jiné datum.";
    } else if (!byId("planLines").children.length) {
        message = "Přidej alespoň jedno jídlo.";
    } else {
        const selected = new Set();
        for (const select of byId("planLines").querySelectorAll("select")) {
            if (selected.has(select.value)) {
                message = "Stejné jídlo je v plánu dvakrát. Počet porcí zadej u jednoho jídla.";
                break;
            }
            selected.add(select.value);
        }
    }
    byId("planFeedback").textContent = message;
    byId("savePlan").disabled = Boolean(message);
}

async function showAvailability() {
    const day = byId("day").value;
    updatePlanFeedback();
    byId("availabilityDay").textContent = byId("day").valueAsDate?.toLocaleDateString("cs-CZ", {timeZone: "UTC"}) || "";
    portionsByRecipe.clear();
    refreshPlanChoices();
    if (!day) {
        list("availability", [], "Den přípravy není zadaný.");
        return;
    }
    const items = await api("availability?day=" + day);
    if (day !== byId("day").value) {
        return;
    }
    for (const item of items) {
        portionsByRecipe.set(item.recipeId, item.portions);
    }
    refreshPlanChoices();
    list("availability", items.map(item => {
        let text = item.name + " · " + portionCount(item.portions);
        if (item.useSoon) {
            text += " · doporučeno kvůli expiraci";
        }
        return text;
    }), "Žádné uložené recepty.");
}

async function showPlan() {
    const id = byId("planChoice").value;
    const plan = mealPlans.find(item => item.id === Number(id));
    byId("preparePlan").disabled = !plan || plan.prepared;
    if (!id) {
        byId("planStatus").textContent = "";
        list("planDetail", [], "Zatím žádný plán.");
        list("shopping", [], "Žádný uložený plán.");
        return;
    }
    if (plan.prepared) {
        byId("planStatus").textContent = "Připraveno · suroviny byly odepsány ze skladu.";
    } else {
        byId("planStatus").textContent = "Naplánováno · zásoby zatím nejsou odepsané ani rezervované.";
    }
    const lines = await api("plans/" + id + "/items");
    list("planDetail", lines.map(item => recipe(item.recipeId).name + " · " + item.portions + " porcí"), "Prázdný plán.");
    if (plan.prepared) {
        list("shopping", [], "Plán už je připravený, nákupní seznam není potřeba.");
        return;
    }
    const missing = await api("plans/" + id + "/shopping");
    list("shopping", missing.map(item => item.name + " · " + item.missing + " " + item.unit), "Zásoby pokrývají celý plán.");
}

function addLine(container, items, label, quantityLabel, step) {
    const row = document.createElement("div");
    row.className = "line";
    const select = document.createElement("select");
    select.required = true;
    select.setAttribute("aria-label", "Položka");
    choices(select, items, label);
    const quantity = document.createElement("input");
    quantity.type = "number";
    quantity.value = "1";
    quantity.min = step;
    quantity.step = step;
    quantity.required = true;
    const caption = document.createElement("label");
    caption.textContent = quantityLabel;
    caption.append(quantity);
    const remove = document.createElement("button");
    remove.type = "button";
    remove.textContent = "Odebrat jídlo";
    remove.onclick = () => {
        row.remove();
        updatePlanFeedback();
    };
    const recipeLabel = document.createElement("label");
    recipeLabel.textContent = "Recept";
    recipeLabel.append(select);
    select.removeAttribute("aria-label");
    row.append(recipeLabel, caption, remove);
    byId(container).append(row);
}

function addRecipeLine() {
    const row = byId("recipeItem").content.firstElementChild.cloneNode(true);
    const name = row.querySelector(".name");
    name.addEventListener("input", () => updateIngredientFields(row));
    row.querySelector(".remove").onclick = () => row.remove();
    byId("recipeLines").append(row);
    updateIngredientFields(row);
}

function findIngredient(name) {
    const normalized = name.trim().toLocaleLowerCase("cs");
    return ingredients.find(item => item.name.toLocaleLowerCase("cs") === normalized);
}

function updateDeliveryFields() {
    const form = byId("deliveryForm");
    const category = form.querySelector(".itemCategory");
    const unit = form.querySelector(".itemUnit");
    const name = byId("deliveryIngredient").value.trim();
    const existing = findIngredient(name);

    if (existing) {
        category.value = existing.categoryId;
        unit.value = existing.unit;
        category.disabled = true;
        unit.disabled = true;
    } else {
        if (category.disabled || !name) {
            category.value = "";
            unit.value = "";
        }
        category.disabled = !name;
        unit.disabled = !name;
    }
}

function updateIngredientFields(row) {
    const name = row.querySelector(".name").value.trim();
    const existing = findIngredient(name);
    const isNew = name.length > 0 && !existing;
    const category = row.querySelector(".itemCategory");
    choices(category, categories, item => item.name);
    row.querySelector(".newFields").hidden = !isNew;
    category.required = isNew;
    row.querySelector(".knownUnit").textContent = "";
    if (existing) {
        row.querySelector(".knownUnit").textContent = existing.unit;
    }
}

function addPlanLine() {
    addLine("planLines", recipes, planRecipeLabel, "Plánovaný počet porcí", "1");
    updatePlanFeedback();
}

async function refresh() {
    ingredients = await api("ingredients");
    recipes = await api("recipes");
    categories = await api("categories");
    choices(byId("category"), categories, item => item.name);
    const names = byId("ingredientNames");
    names.replaceChildren();
    for (const item of ingredients) {
        names.append(new Option(item.name, item.name));
    }
    const deliveryCategory = byId("deliveryForm").querySelector(".itemCategory");
    const previousCategory = deliveryCategory.value;
    choices(deliveryCategory, categories, item => item.name);
    deliveryCategory.prepend(new Option("Bez výběru", ""));
    deliveryCategory.value = previousCategory;
    updateDeliveryFields();
    choices(byId("recipeChoice"), recipes, item => item.name);
    mealPlans = await api("plans");
    if (chooseFreeDay) {
        firstFreeDay();
        chooseFreeDay = false;
    }
    choices(byId("planChoice"), mealPlans, item => {
        if (item.prepared) {
            return item.day + " · připraveno";
        } else {
            return item.day + " · naplánováno";
        }
    });
    for (const row of byId("recipeLines").children) {
        updateIngredientFields(row);
    }
    await showCatalog();
    await showStock();
    await showRecipe();
    await showAvailability();
    await showPlan();
}

function form(id, action) {
    listen(id, "submit", async () => {
        const element = byId(id);
        await action(new FormData(element));
        element.reset();
        await refresh();
        byId("message").textContent = "Uloženo.";
    });
}

form("categoryForm", data => api("categories", {name: data.get("name")}));
form("ingredientForm", data => api("ingredients?storage=" + byId("storage").value, {
    name: data.get("name"), categoryId: Number(byId("category").value),
    unit: data.get("unit"), minimum: Number(data.get("minimum"))
}));
listen("deliveryIngredient", "input", updateDeliveryFields);
form("deliveryForm", data => {
    const name = byId("deliveryIngredient").value.trim();
    const existing = findIngredient(name);
    const delivery = {remaining: Number(data.get("quantity")), expires: data.get("expires")};
    if (existing) {
        delivery.ingredientId = existing.id;
    } else {
        const form = byId("deliveryForm");
        delivery.name = name;
        delivery.categoryId = Number(form.querySelector(".itemCategory").value);
        delivery.unit = form.querySelector(".itemUnit").value;
    }
    return api("deliveries", delivery);
});
form("withdrawForm", data => api("deliveries/" + byId("withdrawDelivery").value
    + "/withdraw?quantity=" + encodeURIComponent(data.get("quantity")), {}));

form("recipeForm", async data => {
    const lines = [];
    for (const row of byId("recipeLines").children) {
        const name = row.querySelector(".name").value.trim();
        const quantity = Number(row.querySelector(".amount").value);
        const existing = findIngredient(name);
        if (existing) {
            lines.push({ingredientId: existing.id, quantity});
        } else {
            lines.push({name, quantity, categoryId: Number(row.querySelector(".itemCategory").value),
                unit: row.querySelector(".itemUnit").value});
        }
    }
    await api("recipes", {name: data.get("name"), instructions: data.get("instructions"),
        servings: Number(data.get("servings")), ingredients: lines});
    byId("recipeLines").replaceChildren();
    addRecipeLine();
});

listen("planForm", "submit", async () => {
    updatePlanFeedback();
    if (byId("savePlan").disabled) return;
    const items = [];
    for (const row of byId("planLines").children) {
        items.push({recipeId: Number(row.querySelector("select").value),
            portions: Number(row.querySelector("input").value)});
    }
    let id;
    byId("savePlan").disabled = true;
    try {
        id = await api("plans", {day: byId("day").value, items});
    } catch (error) {
        byId("planFeedback").textContent = error.message;
        byId("savePlan").disabled = false;
        return;
    }
    const savedDay = byId("availabilityDay").textContent;
    chooseFreeDay = true;
    byId("planLines").replaceChildren();
    addPlanLine();
    await refresh();
    byId("planChoice").value = id;
    await showPlan();
    byId("message").textContent = "Denní plán na " + savedDay + " byl uložen.";
});

function showSection() {
    const links = document.querySelectorAll("nav a");
    let active = document.querySelector("nav a");
    for (const link of links) {
        if (link.hash === location.hash) {
            active = link;
        }
    }
    for (const link of links) {
        const section = byId(link.hash.slice(1));
        section.hidden = link !== active;
        link.removeAttribute("aria-current");
    }
    active.setAttribute("aria-current", "page");
    window.scrollTo(0, 0);
}

window.addEventListener("hashchange", showSection);
showSection();
listen("addRecipeLine", "click", addRecipeLine);
listen("addPlanLine", "click", addPlanLine);
listen("recipeChoice", "change", showRecipe);
listen("planChoice", "change", showPlan);
listen("preparePlan", "click", async () => {
    const id = byId("planChoice").value;
    byId("preparePlan").disabled = true;
    try {
        await api("plans/" + id + "/prepare", {});
        await refresh();
        byId("message").textContent = "Příprava potvrzena. Suroviny byly odepsány ze skladu.";
    } finally {
        const plan = mealPlans.find(item => item.id === Number(byId("planChoice").value));
        byId("preparePlan").disabled = !plan || plan.prepared;
    }
});
listen("storage", "change", showCatalog);
byId("planLines").addEventListener("change", updatePlanFeedback);
listen("day", "change", showAvailability);
byId("day").value = today();
try {
    await refresh();
    addRecipeLine();
    addPlanLine();
} catch (error) {
    byId("message").textContent = error.message;
}
