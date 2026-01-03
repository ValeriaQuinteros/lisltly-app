const Api = {
  async request(path, options = {}) {
    const response = await fetch(path, {
      headers: {
        "Content-Type": "application/json",
        ...(options.headers ?? {}),
      },
      ...options,
    });

    if (response.status === 204) {
      return { ok: true, status: response.status, data: null };
    }

    const text = await response.text();
    const data = text ? safeJsonParse(text) : null;

    if (!response.ok) {
      return { ok: false, status: response.status, data };
    }

    return { ok: true, status: response.status, data };
  },

  listLists({ categoria, q } = {}) {
    const params = new URLSearchParams();
    if (categoria) params.set("categoria", categoria);
    if (q) params.set("q", q);
    const qs = params.toString();
    return this.request(`/api/lists${qs ? `?${qs}` : ""}`, { method: "GET" });
  },

  createList(payload) {
    return this.request("/api/lists", { method: "POST", body: JSON.stringify(payload) });
  },

  getList(id) {
    return this.request(`/api/lists/${id}`, { method: "GET" });
  },

  updateList(id, payload) {
    return this.request(`/api/lists/${id}`, { method: "PUT", body: JSON.stringify(payload) });
  },

  deleteList(id) {
    return this.request(`/api/lists/${id}`, { method: "DELETE" });
  },

  getItems(listId) {
    return this.request(`/api/lists/${listId}/items`, { method: "GET" });
  },

  addItem(listId, payload) {
    return this.request(`/api/lists/${listId}/items`, { method: "POST", body: JSON.stringify(payload) });
  },

  updateItemCompletion(listId, itemId, payload) {
    return this.request(`/api/lists/${listId}/items/${itemId}`, { method: "PATCH", body: JSON.stringify(payload) });
  },
  updateItemProps(listId, itemId, payload) {
    return this.request(`/api/lists/${listId}/items/${itemId}/props`, { method: "PATCH", body: JSON.stringify(payload) });
  },

  deleteItem(listId, itemId) {
    return this.request(`/api/lists/${listId}/items/${itemId}`, { method: "DELETE" });
  },
};

function safeJsonParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return { raw: text };
  }
}

function $(id) {
  return document.getElementById(id);
}

const State = {
  lists: [],
  selectedListId: null,
  search: "",
  category: "",
  detailsById: new Map(),
};

const ui = {
  listsPanel: $("listsPanel"),
  listsPanelMobile: $("listsPanelMobile"),
  homeGrid: $("homeGrid"),
  searchInput: $("searchInput"),
  searchInputMobile: $("searchInputMobile"),
  clearSearchBtn: $("clearSearchBtn"),
  clearSearchBtnMobile: $("clearSearchBtnMobile"),
  categorySelect: $("categorySelect"),
  categorySelectMobile: $("categorySelectMobile"),

  detailEmpty: $("detailEmpty"),
  detailView: $("detailView"),
  detailTitle: $("detailTitle"),
  detailCategory: $("detailCategory"),
  detailDate: $("detailDate"),
  detailDesc: $("detailDesc"),
  itemsPanel: $("itemsPanel"),
  itemsCount: $("itemsCount"),
  addItemForm: $("addItemForm"),
  newItemText: $("newItemText"),
  itemError: $("itemError"),

  createListModal: bootstrap.Modal.getOrCreateInstance($("createListModal")),
  createListForm: $("createListForm"),
  createTitle: $("createTitle"),
  createCategory: $("createCategory"),
  createDate: $("createDate"),
  createDesc: $("createDesc"),
  createListError: $("createListError"),

  editListModal: bootstrap.Modal.getOrCreateInstance($("editListModal")),
  editListForm: $("editListForm"),
  editTitle: $("editTitle"),
  editCategory: $("editCategory"),
  editDate: $("editDate"),
  editDesc: $("editDesc"),
  editListError: $("editListError"),

  editListBtn: $("editListBtn"),
  deleteListBtn: $("deleteListBtn"),

  toastHost: $("toastHost"),
};

function showToast(message, variant = "primary") {
  const toastEl = document.createElement("div");
  toastEl.className = `toast align-items-center text-bg-${variant} border-0`;
  toastEl.setAttribute("role", "alert");
  toastEl.setAttribute("aria-live", "assertive");
  toastEl.setAttribute("aria-atomic", "true");
  toastEl.innerHTML = `
    <div class="d-flex">
      <div class="toast-body">${escapeHtml(message)}</div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
    </div>
  `;
  ui.toastHost.appendChild(toastEl);
  const toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay: 2200 });
  toast.show();
  toastEl.addEventListener("hidden.bs.toast", () => toastEl.remove());
}

function escapeHtml(text) {
  return String(text)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function setAlert(el, message) {
  if (!message) {
    el.classList.add("d-none");
    el.textContent = "";
    return;
  }
  el.classList.remove("d-none");
  el.textContent = message;
}

function setInlineError(el, message) {
  if (!message) {
    el.classList.add("d-none");
    el.textContent = "";
    return;
  }
  el.classList.remove("d-none");
  el.textContent = message;
}

function normalizeText(value) {
  const v = (value ?? "").trim();
  return v.length ? v : null;
}

function formatDate(value) {
  if (!value) return "";
  return `Objetivo: ${value}`;
}

function buildCategories(lists) {
  const set = new Set();
  for (const l of lists) {
    if (l.categoria) set.add(l.categoria);
  }
  return Array.from(set).sort((a, b) => a.localeCompare(b));
}

function renderCategorySelect() {
  const categories = buildCategories(State.lists);
  const selects = [ui.categorySelect, ui.categorySelectMobile];

  for (const select of selects) {
    const previous = select.value;
    select.innerHTML = `<option value="">Todas las categorías</option>`;
    for (const c of categories) {
      const opt = document.createElement("option");
      opt.value = c;
      opt.textContent = c;
      select.appendChild(opt);
    }
    select.value = previous || "";
  }
}

function renderLists() {
  const panels = [ui.listsPanel, ui.listsPanelMobile];
  for (const panel of panels) panel.innerHTML = "";

  if (State.lists.length === 0) {
    for (const panel of panels) {
      const empty = document.createElement("div");
      empty.className = "p-3 text-center text-secondary";
      empty.textContent = "No hay listas todavía.";
      panel.appendChild(empty);
    }
    return;
  }

  for (const l of State.lists) {
    const el = document.createElement("button");
    el.type = "button";
    el.className = `list-group-item list-group-item-action listly-list-item ${l.id === State.selectedListId ? "active" : ""}`;
    el.innerHTML = `
      <div class="d-flex w-100 justify-content-between align-items-start gap-2">
        <div class="me-auto">
          <div class="fw-semibold">${escapeHtml(l.titulo)}</div>
          <div class="small listly-muted">
            ${escapeHtml(l.categoria || "General")}
            ${l.fechaObjetivo ? ` · ${escapeHtml(l.fechaObjetivo)}` : ""}
          </div>
        </div>
        <i class="bi bi-chevron-right listly-muted"></i>
      </div>
    `;
    el.addEventListener("click", () => selectList(l.id, true));

    const el2 = el.cloneNode(true);
    el2.addEventListener("click", () => selectList(l.id, false));

    ui.listsPanel.appendChild(el);
    ui.listsPanelMobile.appendChild(el2);
  }
}

function renderDetail(list) {
  if (!list) {
    ui.detailEmpty.classList.remove("d-none");
    ui.detailView.classList.add("d-none");
    return;
  }

  ui.detailEmpty.classList.add("d-none");
  ui.detailView.classList.remove("d-none");

  ui.detailTitle.textContent = list.titulo ?? "";
  ui.detailCategory.textContent = list.categoria ?? "General";
  const iconEl = document.getElementById("detailIcon");
  if (iconEl) {
    iconEl.className = `bi ${mapCategoryIcon(list.categoria)} fs-4`;
  }
  ui.detailDate.textContent = formatDate(list.fechaObjetivo);
  ui.detailDesc.textContent = list.descripcion ? `· ${list.descripcion}` : "";

  ui.itemsPanel.innerHTML = "";
  const items = list.items ?? [];
  const completed = items.filter((i) => i.completado).length;
  ui.itemsCount.textContent = `${items.length} total · ${completed} completados`;

  if (items.length === 0) {
    const empty = document.createElement("div");
    empty.className = "text-secondary text-center py-4";
    empty.textContent = "No hay ítems. Agrega el primero arriba.";
    ui.itemsPanel.appendChild(empty);
    return;
  }

  for (const item of items) {
    const row = document.createElement("div");
    row.className = "d-flex align-items-center gap-3 border rounded-4 p-3 bg-white listly-item-row";

    const checkbox = document.createElement("input");
    checkbox.type = "checkbox";
    checkbox.className = "form-check-input listly-item-check";
    checkbox.checked = Boolean(item.completado);
    checkbox.addEventListener("change", async () => {
      await toggleItem(item.id, checkbox.checked);
    });

    const text = document.createElement("div");
    text.className = `flex-grow-1 ${item.completado ? "listly-line-through" : ""}`;
    text.textContent = item.texto ?? "";

    const chips = document.createElement("div");
    chips.className = "d-flex align-items-center gap-2";
    const member = document.createElement("span");
    member.className = "listly-avatar-sm";
    member.textContent = (item.integrante?.trim()?.charAt(0) || " ");
    member.title = item.integrante || "Sin integrante";
    member.addEventListener("click", async () => {
      const name = window.prompt("Asignar integrante (deja vacío para limpiar):", item.integrante || "");
      await updateItemProps(item.id, { integrante: normalizeText(name) });
    });
    const status = document.createElement("span");
    status.className = `chip chip-${mapStatusToClass(item.estado)}`;
    status.textContent = item.estado || "Idea";
    status.addEventListener("click", async () => {
      const next = nextStatus(item.estado);
      await updateItemProps(item.id, { estado: next });
    });
    const prio = document.createElement("span");
    prio.className = `chip chip-prio-${item.prioridad ?? 2}`;
    prio.textContent = mapPriorityToText(item.prioridad);
    prio.addEventListener("click", async () => {
      const next = cyclePriority(item.prioridad);
      await updateItemProps(item.id, { prioridad: next });
    });

    chips.appendChild(member);
    chips.appendChild(status);
    chips.appendChild(prio);

    const del = document.createElement("button");
    del.className = "btn btn-outline-danger btn-sm";
    del.type = "button";
    del.innerHTML = `<i class="bi bi-trash"></i>`;
    del.addEventListener("click", async () => {
      await removeItem(item.id);
    });

    row.appendChild(checkbox);
    row.appendChild(text);
    row.appendChild(chips);
    row.appendChild(del);
    ui.itemsPanel.appendChild(row);
  }
}

async function loadLists() {
  const { ok, data, status } = await Api.listLists({
    categoria: normalizeText(State.category),
    q: normalizeText(State.search),
  });

  if (!ok) {
    showToast(`Error cargando listas (${status})`, "danger");
    return;
  }

  State.lists = Array.isArray(data) ? data : [];
  renderCategorySelect();

  if (State.selectedListId && !State.lists.some((l) => l.id === State.selectedListId)) {
    State.selectedListId = null;
    renderDetail(null);
  }

  renderLists();
  renderHomeGrid();
}

async function selectList(id, keepMobileOpen) {
  State.selectedListId = id;
  renderLists();

  if (!keepMobileOpen) {
    const offcanvasEl = $("listsOffcanvas");
    const offcanvas = bootstrap.Offcanvas.getInstance(offcanvasEl);
    offcanvas?.hide();
  }

  const { ok, data, status } = await Api.getList(id);
  if (!ok) {
    showToast(`Error cargando la lista (${status})`, "danger");
    renderDetail(null);
    return;
  }

  renderDetail(data);
}

async function createListFromModal() {
  setAlert(ui.createListError, null);
  const titulo = normalizeText(ui.createTitle.value);
  if (!titulo) {
    setAlert(ui.createListError, "El título es obligatorio.");
    return;
  }

  const payload = {
    titulo,
    categoria: normalizeText(ui.createCategory.value),
    fechaObjetivo: normalizeText(ui.createDate.value),
    descripcion: normalizeText(ui.createDesc.value),
  };

  const { ok, data, status } = await Api.createList(payload);
  if (!ok) {
    setAlert(ui.createListError, `No se pudo crear la lista (${status}).`);
    return;
  }

  ui.createListModal.hide();
  ui.createListForm.reset();
  showToast("Lista creada", "success");
  await loadLists();
  if (data?.id) {
    await selectList(data.id, true);
  }
}

async function updateListFromModal() {
  setAlert(ui.editListError, null);
  if (!State.selectedListId) return;

  const titulo = normalizeText(ui.editTitle.value);
  if (!titulo) {
    setAlert(ui.editListError, "El título es obligatorio.");
    return;
  }

  const payload = {
    titulo,
    categoria: normalizeText(ui.editCategory.value),
    fechaObjetivo: normalizeText(ui.editDate.value),
    descripcion: normalizeText(ui.editDesc.value),
  };

  const { ok, data, status } = await Api.updateList(State.selectedListId, payload);
  if (!ok) {
    setAlert(ui.editListError, `No se pudo guardar (${status}).`);
    return;
  }

  ui.editListModal.hide();
  showToast("Cambios guardados", "success");
  await loadLists();
  renderDetail(data);
}

async function deleteSelectedList() {
  if (!State.selectedListId) return;

  const confirmed = window.confirm("¿Eliminar esta lista y todos sus ítems?");
  if (!confirmed) return;

  const { ok, status } = await Api.deleteList(State.selectedListId);
  if (!ok) {
    showToast(`No se pudo eliminar (${status})`, "danger");
    return;
  }

  State.selectedListId = null;
  showToast("Lista eliminada", "success");
  renderDetail(null);
  await loadLists();
}

async function addItem(text) {
  if (!State.selectedListId) return;
  setInlineError(ui.itemError, null);

  const payload = {
    texto: normalizeText(text),
    integrante: normalizeText($("newItemMember").value),
    estado: $("newItemStatus").value,
    prioridad: Number($("newItemPriority").value)
  };
  if (!payload.texto) {
    setInlineError(ui.itemError, "Escribe algo para agregar.");
    return;
  }

  const { ok, status } = await Api.addItem(State.selectedListId, payload);
  if (!ok) {
    setInlineError(ui.itemError, `No se pudo agregar (${status}).`);
    return;
  }

  ui.newItemText.value = "";
  $("newItemMember").value = "";
  $("newItemStatus").value = "Idea";
  $("newItemPriority").value = "2";
  await selectList(State.selectedListId, true);
}

async function toggleItem(itemId, checked) {
  if (!State.selectedListId) return;
  const { ok, status } = await Api.updateItemCompletion(State.selectedListId, itemId, { completado: checked });
  if (!ok) {
    showToast(`No se pudo actualizar (${status})`, "danger");
    await selectList(State.selectedListId, true);
    return;
  }
  await selectList(State.selectedListId, true);
}

async function removeItem(itemId) {
  if (!State.selectedListId) return;
  const confirmed = window.confirm("¿Eliminar este ítem?");
  if (!confirmed) return;

  const { ok, status } = await Api.deleteItem(State.selectedListId, itemId);
  if (!ok) {
    showToast(`No se pudo eliminar (${status})`, "danger");
    return;
  }
  await selectList(State.selectedListId, true);
}

function bindSearch() {
  const sync = (value) => {
    State.search = value ?? "";
    ui.searchInput.value = State.search;
    ui.searchInputMobile.value = State.search;
  };

  const run = debounce(async () => {
    await loadLists();
  }, 250);

  ui.searchInput.addEventListener("input", (e) => {
    sync(e.target.value);
    run();
  });
  ui.searchInputMobile.addEventListener("input", (e) => {
    sync(e.target.value);
    run();
  });

  ui.clearSearchBtn.addEventListener("click", async () => {
    sync("");
    await loadLists();
  });
  ui.clearSearchBtnMobile.addEventListener("click", async () => {
    sync("");
    await loadLists();
  });
}

function bindCategory() {
  const sync = (value) => {
    State.category = value ?? "";
    ui.categorySelect.value = State.category;
    ui.categorySelectMobile.value = State.category;
  };

  ui.categorySelect.addEventListener("change", async (e) => {
    sync(e.target.value);
    await loadLists();
  });
  ui.categorySelectMobile.addEventListener("change", async (e) => {
    sync(e.target.value);
    await loadLists();
  });
}

function bindForms() {
  ui.createListForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    await createListFromModal();
  });

  ui.editListForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    await updateListFromModal();
  });

  ui.addItemForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    await addItem(ui.newItemText.value);
  });

  ui.editListBtn.addEventListener("click", async () => {
    if (!State.selectedListId) return;
    const { ok, data } = await Api.getList(State.selectedListId);
    if (!ok) return;

    ui.editTitle.value = data.titulo ?? "";
    ui.editCategory.value = data.categoria ?? "";
    ui.editDate.value = data.fechaObjetivo ?? "";
    ui.editDesc.value = data.descripcion ?? "";
    setAlert(ui.editListError, null);
    ui.editListModal.show();
  });

  ui.deleteListBtn.addEventListener("click", async () => {
    await deleteSelectedList();
  });
}

function debounce(fn, waitMs) {
  let t = null;
  return (...args) => {
    if (t) window.clearTimeout(t);
    t = window.setTimeout(() => fn(...args), waitMs);
  };
}

function mapCategoryIcon(cat) {
  const c = (cat || "").toLowerCase();
  if (c.includes("cocina")) return "bi-egg-fried";
  if (c.includes("dormitorio")) return "bi-house-door";
  if (c.includes("living")) return "bi-tv";
  if (c.includes("bañ") || c.includes("bano")) return "bi-droplet";
  if (c.includes("lav")) return "bi-bucket";
  return "bi-check2-square";
}

function orderSpaces(a, b) {
  const order = ["cocina", "dormitorio", "living", "ba", "lav"];
  const ia = order.findIndex((o) => (a.toLowerCase().includes(o)));
  const ib = order.findIndex((o) => (b.toLowerCase().includes(o)));
  const va = ia < 0 ? 999 : ia;
  const vb = ib < 0 ? 999 : ib;
  if (va !== vb) return va - vb;
  return a.localeCompare(b);
}

function renderHomeGrid() {
  const grid = ui.homeGrid;
  if (!grid) return;
  grid.innerHTML = "";
  if (State.lists.length === 0) return;

  const groups = new Map();
  for (const l of State.lists) {
    const k = (l.categoria || "General");
    if (!groups.has(k)) groups.set(k, []);
    groups.get(k).push(l);
  }

  const sortedCats = Array.from(groups.keys()).sort(orderSpaces);
  for (const cat of sortedCats) {
    for (const l of groups.get(cat)) {
      const col = document.createElement("div");
      col.className = "col-12 col-sm-6 col-lg-4";
      const card = document.createElement("button");
      card.type = "button";
      card.className = "w-100 listly-card-home text-start";
      card.addEventListener("click", () => selectList(l.id, true));
      card.innerHTML = `
        <div class="d-flex align-items-center gap-3">
          <span class="listly-card-icon"><i class="bi ${mapCategoryIcon(l.categoria)}"></i></span>
          <div class="flex-grow-1">
            <div class="listly-card-title">${escapeHtml(l.titulo)}</div>
            <div class="small text-secondary">${escapeHtml(l.categoria || "General")}</div>
          </div>
        </div>
        <div class="mt-1 small text-secondary" data-counter></div>
        <div class="mt-2 listly-progress"><div class="listly-progress-bar" style="width:0%"></div></div>
        <div class="mt-2 vstack gap-1" data-items></div>
      `;
      col.appendChild(card);
      grid.appendChild(col);
      fillHomeCardAsync(card, l.id);
    }
  }
}

async function fillHomeCardAsync(cardEl, listId) {
  let detail = State.detailsById.get(listId);
  if (!detail) {
    const { ok, data } = await Api.getList(listId);
    if (!ok) return;
    detail = data;
    State.detailsById.set(listId, detail);
  }
  const items = Array.isArray(detail.items) ? detail.items : [];
  const done = items.filter(i => i.completado).length;
  const pct = items.length ? Math.round((done / items.length) * 100) : 0;
  const bar = cardEl.querySelector(".listly-progress-bar");
  if (bar) bar.style.width = `${pct}%`;
  const host = cardEl.querySelector("[data-items]");
  const cntEl = cardEl.querySelector("[data-counter]");
  if (host) {
    host.innerHTML = "";
    for (const it of items.slice(0, 2)) {
      const line = document.createElement("div");
      line.className = "listly-item-mini";
      line.innerHTML = `
        <span class="${it.completado ? "text-success" : ""}">${escapeHtml(it.texto || "")}</span>
        <span class="chip ${"chip-" + mapStatusToClass(it.estado)}">${escapeHtml(it.estado || "Idea")}</span>
      `;
      host.appendChild(line);
    }
  }
  if (cntEl) {
    cntEl.textContent = `${done}/${items.length}`;
  }
}
function nextStatus(s) {
  const order = ["Idea", "Por comprar", "Comprado"];
  const idx = Math.max(0, order.indexOf(normalizeEstado(s)));
  return order[(idx + 1) % order.length];
}
function normalizeEstado(s) {
  const v = (s || "").toLowerCase();
  if (v.includes("comprado")) return "Comprado";
  if (v.includes("por")) return "Por comprar";
  return "Idea";
}
function mapStatusToClass(s) {
  const v = normalizeEstado(s);
  return v === "Comprado" ? "success" : v === "Por comprar" ? "warning" : "idea";
}
function mapPriorityToText(p) {
  const v = Number(p || 2);
  return v >= 3 ? "Alta" : v <= 1 ? "Baja" : "Media";
}
function cyclePriority(p) {
  const v = Number(p || 2);
  return v >= 3 ? 1 : v <= 1 ? 2 : 3;
}
async function updateItemProps(itemId, patch) {
  if (!State.selectedListId) return;
  const { ok, status } = await Api.updateItemProps(State.selectedListId, itemId, patch);
  if (!ok) {
    showToast(`No se pudo actualizar (${status})`, "danger");
    return;
  }
  await selectList(State.selectedListId, true);
}

async function init() {
  bindSearch();
  bindCategory();
  bindForms();
  await loadLists();
  renderDetail(null);
}

init();
