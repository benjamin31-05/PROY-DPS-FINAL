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

// Función para mostrar mensaje
function mostrarMensaje(mensaje, tipo = 'success') {
    const toast = document.createElement('div');
    toast.classList.add('toast', `toast-${tipo}`);
    toast.textContent = mensaje;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

// Verificar stock disponible
function verificarStock(productId, cantidadSolicitada) {
    const productoEnCarrito = carrito.find(item => item.id === productId);
    const cantidadEnCarrito = productoEnCarrito ? productoEnCarrito.cantidad : 0;
    
    const productoElement = document.querySelector(`[data-product-id="${productId}"]`);
    if (!productoElement) return false;

    const stockDisponible = parseInt(productoElement.getAttribute('data-stock') || '0');
    return (cantidadEnCarrito + cantidadSolicitada) <= stockDisponible;
}

// Agregar producto al carrito con verificación de stock
document.querySelectorAll('.add-to-cart').forEach(button => {
    button.addEventListener('click', function() {
        const productId = this.getAttribute('data-product-id');
        const productName = this.getAttribute('data-product-name');
        const productPrice = parseFloat(this.getAttribute('data-product-price'));
        const productImage = this.getAttribute('data-product-image');
        const stockDisponible = parseInt(this.getAttribute('data-stock') || '0');

        // Verificar si hay stock disponible
        if (stockDisponible === 0) {
            mostrarMensaje('Producto agotado', 'error');
            return false;
        }

        // Buscar si el producto ya está en el carrito
        const productoExistente = carrito.find(item => item.id === productId);
        
        if (productoExistente) {
            // Verificar si se puede agregar una unidad más
            if (productoExistente.cantidad + 1 > stockDisponible) {
                mostrarMensaje(`Solo hay ${stockDisponible} unidades disponibles`, 'error');
                return false;
            }
            productoExistente.cantidad += 1;
            total += productPrice;
        } else {
            // Agregar nuevo producto al carrito
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
        mostrarCarrito();
        mostrarMensaje('Producto agregado al carrito');
        return true;
    });
});

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
}

// Modificar la cantidad de un producto con verificación de stock
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

// Actualizar la cantidad manualmente desde el input con verificación de stock
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