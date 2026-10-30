const form = document.querySelector("#transaction-form");
const categoryForm = document.querySelector("#category-form");
const categorySelect = document.querySelector("#category");
const monthFilter = document.querySelector("#month-filter");
const transactionList = document.querySelector("#transaction-list");
const formMessage = document.querySelector("#form-message");
const submitButton = document.querySelector("#submit-button");
const cancelEditButton = document.querySelector("#cancel-edit");
const categoryMessage = document.querySelector("#category-message");
const categorySubmitButton = document.querySelector("#category-submit");
const categoryCancelEditButton = document.querySelector("#category-cancel-edit");
const categoryList = document.querySelector("#category-list");

const currency = new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL"
});

let transactions = [];
let editingId = null;
let editingCategoryId = null;

function currentMonth() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

function localDateValue() {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
}

function formatDate(date) {
    return new Intl.DateTimeFormat("pt-BR", { timeZone: "UTC" })
        .format(new Date(`${date}T00:00:00Z`));
}

function showListMessage(message) {
    transactionList.replaceChildren();
    const paragraph = document.createElement("p");
    paragraph.className = "list-message";
    paragraph.textContent = message;
    transactionList.append(paragraph);
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json", ...options.headers },
        ...options
    });

    if (!response.ok) {
        const detail = await response.text();
        let message = detail;
        try {
            message = JSON.parse(detail).mensagem || detail;
        } catch {
            // Keep the response text when the server does not return JSON.
        }
        throw new Error(message || `Erro ${response.status} ao acessar ${url}`);
    }

    if (response.status === 204) {
        return null;
    }

    const body = await response.text();
    return body ? JSON.parse(body) : null;
}

async function loadCategories(selectedCategoryId = categorySelect.value) {
    try {
        const categories = await request("/categorias");
        renderCategories(categories);
        categorySelect.replaceChildren();

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.disabled = true;
        placeholder.selected = true;
        placeholder.textContent = categories.length ? "Selecione" : "Cadastre uma categoria";
        categorySelect.append(placeholder);

        for (const category of categories) {
            const option = document.createElement("option");
            option.value = category.id;
            option.textContent = category.nome;
            categorySelect.append(option);
        }

        if (selectedCategoryId && categories.some(category => String(category.id) === String(selectedCategoryId))) {
            categorySelect.value = selectedCategoryId;
        }
        categorySelect.disabled = categories.length === 0;
        return true;
    } catch (error) {
        categorySelect.replaceChildren(new Option("Erro ao carregar categorias", ""));
        categorySelect.disabled = true;
        categoryList.replaceChildren();
        const errorMessage = document.createElement("p");
        errorMessage.className = "category-list-message";
        errorMessage.textContent = "Não foi possível carregar as categorias.";
        categoryList.append(errorMessage);
        formMessage.textContent = `Não foi possível carregar as categorias. ${error.message}`;
        return false;
    }
}

function renderCategories(categories) {
    categoryList.replaceChildren();
    if (categories.length === 0) {
        const emptyMessage = document.createElement("p");
        emptyMessage.className = "category-list-message";
        emptyMessage.textContent = "Nenhuma categoria cadastrada.";
        categoryList.append(emptyMessage);
        return;
    }

    for (const category of categories) {
        const item = document.createElement("div");
        item.className = "category-item";
        const name = document.createElement("p");
        name.className = "category-name";
        name.textContent = category.nome;

        const actions = document.createElement("div");
        actions.className = "category-actions";
        const editButton = document.createElement("button");
        editButton.className = "edit-button";
        editButton.type = "button";
        editButton.textContent = "✎";
        editButton.title = `Editar categoria ${category.nome}`;
        editButton.setAttribute("aria-label", `Editar categoria ${category.nome}`);
        editButton.addEventListener("click", () => startEditingCategory(category));

        const deleteButton = document.createElement("button");
        deleteButton.className = "delete-button";
        deleteButton.type = "button";
        deleteButton.textContent = "×";
        deleteButton.title = `Excluir categoria ${category.nome}`;
        deleteButton.setAttribute("aria-label", `Excluir categoria ${category.nome}`);
        deleteButton.addEventListener("click", () => deleteCategory(category, deleteButton));

        actions.append(editButton, deleteButton);
        item.append(name, actions);
        categoryList.append(item);
    }
}

categoryForm.addEventListener("submit", async event => {
    event.preventDefault();
    categoryMessage.classList.remove("success");
    categoryMessage.textContent = "";
    categorySubmitButton.disabled = true;

    const formData = new FormData(categoryForm);
    const nome = formData.get("nome").trim();
    const categoryId = editingCategoryId;

    try {
        const category = await request(
            categoryId === null ? "/categorias" : `/categorias/${categoryId}`,
            {
                method: categoryId === null ? "POST" : "PUT",
                body: JSON.stringify({ nome })
            });
        resetCategoryForm();
        const categoriesLoaded = await loadCategories(category.id);
        if (!categoriesLoaded) {
            const action = categoryId === null ? "criada" : "atualizada";
            categoryMessage.textContent = `Categoria ${action}, mas não foi possível atualizar a seleção. ${formMessage.textContent}`;
            return;
        }
        if (categoryId !== null) {
            await loadTransactions();
        }
        categoryMessage.classList.add("success");
        categoryMessage.textContent = categoryId === null
            ? `Categoria "${category.nome}" criada e selecionada.`
            : `Categoria atualizada para "${category.nome}".`;
    } catch (error) {
        const action = categoryId === null ? "criar" : "atualizar";
        categoryMessage.textContent = `Não foi possível ${action} a categoria. ${error.message}`;
    } finally {
        categorySubmitButton.disabled = false;
    }
});

function startEditingCategory(category) {
    editingCategoryId = category.id;
    categoryForm.elements.nome.value = category.nome;
    document.querySelector("#category-title").textContent = "Editar categoria";
    categorySubmitButton.innerHTML = '<span aria-hidden="true">✓</span> Salvar categoria';
    categoryCancelEditButton.hidden = false;
    categoryMessage.classList.remove("success");
    categoryMessage.textContent = "";
    document.querySelector("#category-name").focus();
}

function resetCategoryForm() {
    editingCategoryId = null;
    categoryForm.reset();
    document.querySelector("#category-title").textContent = "Categorias";
    categorySubmitButton.innerHTML = '<span aria-hidden="true">＋</span> Criar categoria';
    categoryCancelEditButton.hidden = true;
}

async function deleteCategory(category, button) {
    if (!window.confirm(`Excluir a categoria "${category.nome}"?`)) {
        return;
    }

    button.disabled = true;
    categoryMessage.classList.remove("success");
    try {
        await request(`/categorias/${category.id}`, { method: "DELETE" });
        await loadCategories();
        await loadTransactions();
        categoryMessage.classList.add("success");
        categoryMessage.textContent = `Categoria "${category.nome}" excluída.`;
    } catch (error) {
        button.disabled = false;
        categoryMessage.textContent = `Não foi possível excluir a categoria. ${error.message}`;
    }
}

categoryCancelEditButton.addEventListener("click", resetCategoryForm);
categoryForm.addEventListener("input", () => categoryMessage.classList.remove("success"));

function updateSummary(summary, count) {
    document.querySelector("#income-total").textContent = currency.format(summary.receitas);
    document.querySelector("#expense-total").textContent = currency.format(summary.despesas);
    document.querySelector("#balance-total").textContent = currency.format(summary.saldo);
    document.querySelector("#transaction-count").textContent =
        `${count} ${count === 1 ? "registro" : "registros"}`;
}

function renderTransactions() {
    transactionList.replaceChildren();

    if (transactions.length === 0) {
        showListMessage("Nenhuma transação neste mês. Que tal adicionar a primeira?");
        return;
    }

    for (const transaction of transactions) {
        const row = document.createElement("article");
        row.className = "transaction-row";

        const main = document.createElement("div");
        main.className = "transaction-main";

        const icon = document.createElement("span");
        icon.className = `transaction-icon${transaction.tipo === "DESPESA" ? " expense" : ""}`;
        icon.setAttribute("aria-hidden", "true");
        icon.textContent = transaction.tipo === "RECEITA" ? "↗" : "↘";

        const details = document.createElement("div");
        details.style.minWidth = "0";
        const description = document.createElement("p");
        description.className = "transaction-description";
        description.textContent = transaction.descricao;
        const meta = document.createElement("p");
        meta.className = "transaction-meta";
        meta.textContent = `${formatDate(transaction.data)} · ${transaction.categoriaNome ?? "Sem categoria"}`;
        details.append(description, meta);
        main.append(icon, details);

        const amount = document.createElement("span");
        amount.className = `transaction-amount${transaction.tipo === "DESPESA" ? " expense" : ""}`;
        amount.textContent = `${transaction.tipo === "DESPESA" ? "−" : "+"} ${currency.format(transaction.valor)}`;

        const deleteButton = document.createElement("button");
        deleteButton.className = "delete-button";
        deleteButton.type = "button";
        deleteButton.textContent = "×";
        deleteButton.title = `Excluir ${transaction.descricao}`;
        deleteButton.setAttribute("aria-label", `Excluir transação ${transaction.descricao}`);
        deleteButton.addEventListener("click", () => deleteTransaction(transaction, deleteButton));

        const editButton = document.createElement("button");
        editButton.className = "edit-button";
        editButton.type = "button";
        editButton.textContent = "✎";
        editButton.title = `Editar ${transaction.descricao}`;
        editButton.setAttribute("aria-label", `Editar transação ${transaction.descricao}`);
        editButton.addEventListener("click", () => startEditing(transaction));

        row.append(main, amount, editButton, deleteButton);
        transactionList.append(row);
    }
}

async function loadTransactions() {
    showListMessage("Carregando transações...");
    try {
        const [year, month] = monthFilter.value.split("-");
        const params = `ano=${encodeURIComponent(year)}&mes=${encodeURIComponent(month)}`;
        const [results, summary] = await Promise.all([
            request(`/transacoes?${params}`),
            request(`/transacoes/resumo?${params}`)
        ]);
        transactions = results;
        updateSummary(summary, transactions.length);
        renderTransactions();
    } catch (error) {
        showListMessage(`Não foi possível carregar as transações. ${error.message}`);
        console.error("Erro ao carregar transações:", error);
    }
}

function startEditing(transaction) {
    editingId = transaction.id;
    form.elements.descricao.value = transaction.descricao;
    form.elements.valor.value = transaction.valor;
    form.elements.data.value = transaction.data;
    form.elements.tipo.value = transaction.tipo;
    form.elements.categoria.value = transaction.categoriaId;
    submitButton.innerHTML = '<span aria-hidden="true">✓</span> Salvar alterações';
    cancelEditButton.hidden = false;
    formMessage.textContent = "";
    document.querySelector("#form-title").textContent = "Editar transação";
    form.scrollIntoView({ behavior: "smooth", block: "center" });
}

function resetForm() {
    editingId = null;
    form.reset();
    document.querySelector("#date").value = localDateValue();
    submitButton.innerHTML = '<span aria-hidden="true">＋</span> Salvar transação';
    cancelEditButton.hidden = true;
    document.querySelector("#form-title").textContent = "Adicionar transação";
}

async function deleteTransaction(transaction, button) {
    if (!window.confirm(`Excluir a transação "${transaction.descricao}"?`)) {
        return;
    }

    button.disabled = true;
    try {
        await request(`/transacoes/${transaction.id}`, { method: "DELETE" });
        await loadTransactions();
    } catch (error) {
        button.disabled = false;
        window.alert(`Não foi possível excluir a transação. ${error.message}`);
    }
}

form.addEventListener("submit", async event => {
    event.preventDefault();
    formMessage.textContent = "";
    submitButton.disabled = true;

    const data = new FormData(form);
    const transaction = {
        descricao: data.get("descricao").trim(),
        valor: Number(data.get("valor")),
        data: data.get("data"),
        tipo: data.get("tipo"),
        categoriaId: Number(data.get("categoria"))
    };

    try {
        await request(editingId === null ? "/transacoes" : `/transacoes/${editingId}`, {
            method: editingId === null ? "POST" : "PUT",
            body: JSON.stringify(transaction)
        });
        resetForm();
        monthFilter.value = transaction.data.slice(0, 7);
        await loadTransactions();
    } catch (error) {
        formMessage.textContent = `Não foi possível salvar a transação. ${error.message}`;
    } finally {
        submitButton.disabled = false;
    }
});

cancelEditButton.addEventListener("click", resetForm);
monthFilter.addEventListener("change", loadTransactions);
monthFilter.value = currentMonth();
document.querySelector("#date").value = localDateValue();

loadCategories();
loadTransactions();
