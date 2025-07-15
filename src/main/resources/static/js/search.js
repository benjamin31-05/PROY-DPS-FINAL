// Archivo: header-search.js

let debounceTimer;

function debounceSearch() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(buscarProductos, 300); // Espera 300ms antes de buscar
}

function buscarProductos() {
    const input = document.getElementById('searchInput').value;
    if (input.trim() === '') {
        document.getElementById('searchResults').innerHTML = ''; // Limpiar resultados si está vacío
        return;
    }

    fetch(`/api/productos/buscar?nombre=${encodeURIComponent(input)}`)
        .then(response => response.json())
        .then(productos => mostrarResultados(productos))
        .catch(error => console.error('Error buscando productos:', error));
}

function mostrarResultados(productos) {
    const resultsDiv = document.getElementById('searchResults');
    resultsDiv.innerHTML = ''; // Limpiar resultados previos

    if (productos.length === 0) {
        resultsDiv.innerHTML = '<p>No se encontraron productos.</p>';
        return;
    }

    productos.forEach(producto => {
        const div = document.createElement('div');
        div.className = 'search-result-item';
        div.innerHTML = `
            <img src="${producto.imageUrl}" alt="${producto.nombreProducto}">
            <div>
                <h4>${producto.nombreProducto}</h4>
                <p>${producto.descripcion}</p>
                <span>Precio: $${producto.precio}</span>
            </div>
        `;
        resultsDiv.appendChild(div);
    });
}
