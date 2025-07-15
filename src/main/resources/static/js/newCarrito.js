const carrito = [];
let total = 0;

// Mostrar el carrito
function mostrarCarrito() {
    const carritoPanel = document.getElementById('carrito-panel');
    carritoPanel.classList.add('active');
}

// Cerrar el carrito
function cerrarCarrito() {
    const carritoPanel = document.getElementById('carrito-panel');
    carritoPanel.classList.remove('active');
}

// Función para mostrar mensaje de error/éxito
function mostrarMensaje(mensaje, tipo = 'success') {
    const toast = document.createElement('div');
    toast.classList.add('toast', `toast-${tipo}`);
    toast.textContent = mensaje;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Función para verificar el stock disponible
function verificarStock(productId, cantidadSolicitada) {
    const productoEnCarrito = carrito.find(item => item.id === productId);
    const cantidadEnCarrito = productoEnCarrito ? productoEnCarrito.cantidad : 0;
    
    // Obtener el elemento del producto en el DOM
    const productoElement = document.querySelector(`[data-product-id="${productId}"]`);
    if (!productoElement) return false;

    const stockDisponible = parseInt(productoElement.getAttribute('data-stock') || '0');
    
    // Verificar si la cantidad total (carrito + solicitada) no excede el stock
    return (cantidadEnCarrito + cantidadSolicitada) <= stockDisponible;
}

// Función para obtener el stock disponible de un producto
function obtenerStockDisponible(productId) {
    const productoElement = document.querySelector(`[data-product-id="${productId}"]`);
    return parseInt(productoElement.getAttribute('data-stock') || '0');
}

// Función para agregar producto al carrito
function agregarAlCarrito(productId, productName, productPrice, productImage, cantidad = 1) {
    // Verificar stock antes de agregar
    if (!verificarStock(productId, cantidad)) {
        mostrarMensaje('No hay suficiente stock disponible', 'error');
        return false;
    }
    
    const productoExistente = carrito.find(item => item.id === productId);
    const stockDisponible = obtenerStockDisponible(productId);
    
    if (productoExistente) {
        // Verificar que no exceda el stock disponible
        if (productoExistente.cantidad + cantidad > stockDisponible) {
            mostrarMensaje(`Solo hay ${stockDisponible} unidades disponibles`, 'error');
            return false;
        }
        productoExistente.cantidad += cantidad;
        total += productPrice * cantidad;
    } else {
        carrito.push({
            id: productId,
            nombre: productName,
            precio: productPrice,
            imagen: productImage,
            cantidad: cantidad,
            stockMaximo: stockDisponible
        });
        total += productPrice * cantidad;
    }
    
    actualizarCarrito();
    mostrarCarrito();
    mostrarMensaje('Producto agregado al carrito');
    return true;
}

// Event listeners para los botones de "Añadir al carrito" en la lista de productos
document.querySelectorAll('.products-list .add-to-cart').forEach(button => {
    button.addEventListener('click', function() {
        const productId = this.getAttribute('data-product-id');
        const productName = this.getAttribute('data-product-name');
        const productPrice = parseFloat(this.getAttribute('data-product-price'));
        const productImage = this.getAttribute('data-product-image');
        
        agregarAlCarrito(productId, productName, productPrice, productImage);
    });
});

// Event listener para el formulario en la página de detalles del producto
const detallesProductoForm = document.querySelector('.product-details');
if (detallesProductoForm) {
    detallesProductoForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const submitButton = this.querySelector('button[type="submit"]');
        const cantidad = parseInt(this.querySelector('#quantity').value, 10);
        
        const productId = submitButton.getAttribute('data-product-id');
        const productName = submitButton.getAttribute('data-product-name');
        const productPrice = parseFloat(submitButton.getAttribute('data-product-price'));
        const productImage = submitButton.getAttribute('data-product-image');
        
        agregarAlCarrito(productId, productName, productPrice, productImage, cantidad);
    });
}

// Actualizar la visualización del carrito
function actualizarCarrito() {
    const carritoLista = document.getElementById('carrito-lista');
    carritoLista.innerHTML = '';
    
    carrito.forEach((producto, index) => {
        const li = document.createElement('li');
        li.classList.add('producto-en-carrito');
        li.innerHTML = `
            <img src="${producto.imagen}" alt="${producto.nombre}">
            <div class="producto-detalles">
                <h4>${producto.nombre}</h4>
                <p class="producto-precio">S/ ${(producto.precio * producto.cantidad).toFixed(2)}</p>
                <div class="cantidad-producto">
                    <button onclick="modificarCantidad(${index}, -1)">-</button>
                    <input type="number" 
                           value="${producto.cantidad}" 
                           min="1" 
                           max="${producto.stockMaximo}"
                           onchange="actualizarCantidad(${index}, this.value)">
                    <button onclick="modificarCantidad(${index}, 1)" 
                            ${producto.cantidad >= producto.stockMaximo ? 'disabled' : ''}>+</button>
                </div>
                <p class="stock-info">Stock disponible: ${producto.stockMaximo}</p>
            </div>
            <span class="eliminar-producto" onclick="eliminarProducto(${index})">&times;</span>
        `;
        carritoLista.appendChild(li);
    });
    
    document.getElementById('total').textContent = total.toFixed(2);
    
    // Actualizar contador de items en el carrito si existe
    const carritoCounter = document.querySelector('.cart-counter');
    if (carritoCounter) {
        const totalItems = carrito.reduce((sum, item) => sum + item.cantidad, 0);
        carritoCounter.textContent = totalItems;
        carritoCounter.style.display = totalItems > 0 ? 'block' : 'none';
    }
}

// Modificar la cantidad de un producto
function modificarCantidad(index, cambio) {
    const producto = carrito[index];
    const nuevaCantidad = producto.cantidad + cambio;
    
    if (nuevaCantidad > producto.stockMaximo) {
        mostrarMensaje(`Solo hay ${producto.stockMaximo} unidades disponibles`, 'error');
        return;
    }
    
    if (nuevaCantidad > 0) {
        producto.cantidad = nuevaCantidad;
        total += producto.precio * cambio;
        actualizarCarrito();
    }
}

// Actualizar la cantidad manualmente desde el input
function actualizarCantidad(index, cantidad) {
    const producto = carrito[index];
    const cantidadNumerica = parseInt(cantidad, 10);
    
    if (cantidadNumerica > producto.stockMaximo) {
        mostrarMensaje(`Solo hay ${producto.stockMaximo} unidades disponibles`, 'error');
        actualizarCarrito(); // Restaurar la visualización correcta
        return;
    }
    
    if (cantidadNumerica > 0) {
        total += (cantidadNumerica - producto.cantidad) * producto.precio;
        producto.cantidad = cantidadNumerica;
        actualizarCarrito();
    }
}

// Eliminar un producto del carrito
function eliminarProducto(index) {
    total -= carrito[index].precio * carrito[index].cantidad;
    carrito.splice(index, 1);
    actualizarCarrito();
}

// Event listener para el botón de comprar
document.getElementById('comprar-carrito')?.addEventListener('click', function() {
    window.location.href = 'pago.html';
});