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

// Función para agregar/quitar de favoritos
function toggleFavorito(heartIcon, productId, productName, productPrice, productImage, stock) {
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
            stock: stock
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
    const producto = favoritos.find(item => item.id === productId);
    if (!producto) return false;

    const productoEnCarrito = carrito.find(item => item.id === productId);
    const cantidadEnCarrito = productoEnCarrito ? productoEnCarrito.cantidad : 0;
    
    return (cantidadEnCarrito + cantidadSolicitada) <= producto.stock;
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
        
        let productHTML = `
            <img src="${producto.imagen}" alt="${producto.nombre}">
            <div class="producto-detalles">
                <h4>${producto.nombre}</h4>
                <p class="producto-precio">S/ ${producto.precio.toFixed(2)}</p>`;

        if (producto.stock > 0) {
            productHTML += `
                <button class="add-to-cart-from-favorites"
                        data-product-id="${producto.id}"
                        data-product-name="${producto.nombre}"
                        data-product-price="${producto.precio}"
                        data-product-image="${producto.imagen}"
                        data-stock="${producto.stock}">
                    Añadir al carrito
                </button>
                <p class="stock-info">Stock disponible: ${producto.stock}</p>`;
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

    // Agregar event listeners a los botones con verificación de stock
    document.querySelectorAll('.add-to-cart-from-favorites').forEach(button => {
        button.addEventListener('click', function() {
            const productId = this.getAttribute('data-product-id');
            const productName = this.getAttribute('data-product-name');
            const productPrice = parseFloat(this.getAttribute('data-product-price'));
            const productImage = this.getAttribute('data-product-image');
            const stockDisponible = parseInt(this.getAttribute('data-stock'));
            
            // Verificar stock antes de agregar
            if (!verificarStockFavoritos(productId, 1)) {
                mostrarMensaje(`No hay suficiente stock disponible`, 'error');
                return;
            }
            
            const productoExistente = carrito.find(item => item.id === productId);
            
            if (productoExistente) {
                if (productoExistente.cantidad + 1 > stockDisponible) {
                    mostrarMensaje(`Solo hay ${stockDisponible} unidades disponibles`, 'error');
                    return;
                }
                productoExistente.cantidad += 1;
                total += productPrice;
            } else {
                carrito.push({
                    id: productId, 
                    nombre: productName, 
                    precio: productPrice, 
                    imagen: productImage, 
                    cantidad: 1,
                    stockMaximo: stockDisponible
                });
                total += productPrice;
            }
            
            actualizarCarrito();
            cerrarFavoritos();
            mostrarCarrito();
            mostrarMensaje('Producto agregado al carrito');
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
document.querySelectorAll('.heart-icon').forEach(heartIcon => {
    const productCard = heartIcon.closest('.product-card');
    const productId = productCard.getAttribute('data-product-id');
    const productName = productCard.querySelector('.product-title').textContent;
    const productPrice = parseFloat(productCard.querySelector('.product-price').textContent.replace('S/', ''));
    const productImage = productCard.querySelector('.product-image').style.backgroundImage.slice(5, -2);
    const stock = parseInt(productCard.querySelector('.add-to-cart').getAttribute('data-stock'));

    // Si el producto ya está en favoritos, mostrar el corazón lleno
    if (estaEnFavoritos(productId)) {
        heartIcon.textContent = '❤️';
    }

    heartIcon.addEventListener('click', function() {
        toggleFavorito(this, productId, productName, productPrice, productImage, stock);
    });
});
