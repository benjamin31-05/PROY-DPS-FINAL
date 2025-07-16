// Array para almacenar los favoritos
const favoritos = [];

// Mostrar panel de favoritos
function mostrarFavoritos() {
    const favoritosPanel = document.getElementById('favoritos-panel');
    favoritosPanel.classList.add('active');
}

// Función que muestra el panel del carrito
function mostrarCarrito() {
    const carritoPanel = document.getElementById('carrito-panel');
    carritoPanel.classList.add('active');
}

// Cerrar panel de favoritos
function cerrarFavoritos() {
    const favoritosPanel = document.getElementById('favoritos-panel');
    favoritosPanel.classList.remove('active');
}

// Función para mostrar mensaje
function mostrarMensaje(mensaje, tipo = 'success') {
    const toast = document.createElement('div');
    toast.classList.add('toast', `toast-${tipo}`);
    toast.textContent = mensaje;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Función para verificar si un producto está en favoritos
function estaEnFavoritos(productId) {
    return favoritos.some(item => item.id === productId);
}

// Función para obtener el stock actual del producto desde el DOM
function obtenerStockActual(productId) {
    const productCard = document.querySelector(`[data-product-id="${productId}"]`);
    if (!productCard) return 0;
    
    // Verificar si tiene el badge de agotado
    const agotadoBadge = productCard.querySelector('.agotado-badge');
    if (agotadoBadge) return 0;
    
    // Si no está agotado, obtener el stock desde el atributo data o desde el servidor
    // Por ahora, si no hay badge de agotado, asumimos que hay stock disponible
    const bajoStockBadge = productCard.querySelector('.bajo-stock-badge');
    if (bajoStockBadge) {
        // Si tiene badge de bajo stock, significa que hay entre 1-5 unidades
        return 5; // Valor por defecto para stock bajo
    }
    
    // Si no tiene ningún badge, asumimos stock normal
    return 10; // Valor por defecto para stock normal
}

// Función para agregar/quitar de favoritos
function toggleFavorito(heartIcon, productId, productName, productPrice, productImage) {
    const stockActual = obtenerStockActual(productId);
    
    if (estaEnFavoritos(productId)) {
        // Quitar de favoritos
        const index = favoritos.findIndex(item => item.id === productId);
        favoritos.splice(index, 1);
        heartIcon.textContent = '🤍';
        mostrarMensaje('Producto eliminado de favoritos');
    } else {
        // Agregar a favoritos
        favoritos.push({
            id: productId,
            nombre: productName,
            precio: productPrice,
            imagen: productImage,
            stock: stockActual
        });
        heartIcon.textContent = '❤️';
        mostrarMensaje('Producto agregado a favoritos');
    }
    actualizarFavoritos();
    mostrarFavoritos();
    actualizarContadorFavoritos();
}

// Función para verificar stock desde favoritos
function verificarStockFavoritos(productId, cantidadSolicitada) {
    const stockActual = obtenerStockActual(productId);
    if (stockActual === 0) return false;

    const productoEnCarrito = carrito.find(item => item.id === productId);
    const cantidadEnCarrito = productoEnCarrito ? productoEnCarrito.cantidad : 0;
    
    return (cantidadEnCarrito + cantidadSolicitada) <= stockActual;
}

// Función para actualizar el contador de favoritos
function actualizarContadorFavoritos() {
    const favoritosCounter = document.querySelector('.favoritos-counter');
    if (favoritosCounter) {
        favoritosCounter.textContent = favoritos.length;
        favoritosCounter.style.display = favoritos.length > 0 ? 'block' : 'none';
    }
}

// Función para actualizar la lista de favoritos con manejo de stock
function actualizarFavoritos() {
    const favoritosLista = document.getElementById('favoritos-lista');
    favoritosLista.innerHTML = '';

    favoritos.forEach((producto, index) => {
        const li = document.createElement('li');
        li.classList.add('producto-en-favoritos');
        
        // Obtener stock actual del producto
        const stockActual = obtenerStockActual(producto.id);
        
        let productHTML = `
            <img src="${producto.imagen}" alt="${producto.nombre}">
            <div class="producto-detalles">
                <h4>${producto.nombre}</h4>
                <p class="producto-precio">S/ ${producto.precio.toFixed(2)}</p>`;

        if (stockActual > 0) {
            productHTML += `
                <button class="view-product-details"
                        data-product-id="${producto.id}">
                    Ver más info
                </button>
                <p class="stock-info">Stock disponible: ${stockActual}</p>`;
        } else {
            productHTML += `
                <div class="agotado-mensaje">
                    Producto agotado
                </div>`;
        }

        productHTML += `
            </div>
            <span class="eliminar-favorito" onclick="eliminarFavorito(${index})">&times;</span>`;

        li.innerHTML = productHTML;
        favoritosLista.appendChild(li);
    });

    // Agregar event listeners a los botones de ver más info
    document.querySelectorAll('.view-product-details').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            // Redirigir a la página de especificaciones del producto
            window.location.href = `/productoHome/${productId}`;
        });
    });
}

function eliminarFavorito(index) {
    const producto = favoritos[index];
    const heartIcon = document.querySelector(`.product-card[data-product-id="${producto.id}"] .heart-icon`);
    if (heartIcon) {
        heartIcon.textContent = '🤍';
    }
    favoritos.splice(index, 1);
    actualizarFavoritos();
    actualizarContadorFavoritos();
    mostrarMensaje('Producto eliminado de favoritos');
}

// Inicializar event listeners para los iconos de corazón
document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.heart-icon').forEach(heartIcon => {
        const productCard = heartIcon.closest('.product-card');
        const productId = productCard.getAttribute('data-product-id');
        const productName = productCard.querySelector('.product-title').textContent;
        const productPrice = parseFloat(productCard.querySelector('.product-price').textContent.replace('S/', ''));
        const productImage = productCard.querySelector('.product-image').style.backgroundImage.slice(5, -2);

        // Si el producto ya está en favoritos, mostrar el corazón lleno
        if (estaEnFavoritos(productId)) {
            heartIcon.textContent = '❤️';
        }

        heartIcon.addEventListener('click', function() {
            toggleFavorito(this, productId, productName, productPrice, productImage);
        });
    });
});