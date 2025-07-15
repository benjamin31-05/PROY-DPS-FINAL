//document.addEventListener('DOMContentLoaded', function () {
//    document.querySelectorAll('.heart-icon').forEach(icon => {
//        icon.addEventListener('click', function (e) {
//            e.preventDefault();
//            if (this.textContent === '🤍') {
//                this.textContent = '❤️';
//                showToast("Producto agregado correctamente a la lista de favoritos");
//            } else {
//                this.textContent = '🤍';
//                showToast("Producto eliminado de la lista de favoritos");
//            }
//            this.classList.toggle('active');
//        });
//    });
//});

//Funcionalidad del modal al momento de darle click al ojo
//document.querySelectorAll('.eye-icon').forEach(icon => {
//    icon.addEventListener('click', function (e) {
//        e.preventDefault();
//        const productId = this.closest('.product-card').dataset.productId;
//        showModal(productId);
//    });
//});

//FUNCIONALIDAD DE LOS MODALS DE LOS PRODUCTOS
function showModal(productId) {
    const modal = document.querySelector('.modal');
    const iframe = document.getElementById('modal-content');

    // Lógica para determinar la URL del iframe según el productId
    let url;
    switch (productId) {
        case '1':
            url = 'Otros1.html';
            break;
        case '2':
            url = 'Otros2.html';
            break;
        case '3':
            url = 'Otros3.html';
            break;
            // Añade más casos según los productos que tengas
        default:
            url = 'default.html'; // Página por defecto si no se encuentra el productId
    }

    iframe.src = `${url}?id=${productId}`;
    modal.style.display = 'block';
    document.querySelector('.wrap').classList.add('blur-background');
}

document.querySelector('.close-modal').addEventListener('click', function () {
    document.querySelector('.modal').style.display = 'none';
    document.querySelector('.wrap').classList.remove('blur-background');
});

function showToast(message) {
    const toast = document.querySelector('.toast');
    toast.textContent = message;
    toast.style.opacity = '1';
    setTimeout(() => {
        toast.style.opacity = '0';
    }, 3000);
}


// Funcionalidad de ordenamiento
const sortSelect = document.getElementById('sort-select');
sortSelect.addEventListener('change', sortProducts);

function sortProducts() {
    const productsList = document.querySelector('.products-list');
    const products = Array.from(productsList.querySelectorAll('.product-card'));

    products.sort((a, b) => {
        const sortBy = sortSelect.value;
        switch (sortBy) {
            case 'price-asc':
                return getPriceValue(a) - getPriceValue(b);
            case 'price-desc':
                return getPriceValue(b) - getPriceValue(a);
            case 'name-asc':
                return getProductName(a).localeCompare(getProductName(b));
            case 'name-desc':
                return getProductName(b).localeCompare(getProductName(a));
            default:
                return 0;
        }
    });

    productsList.innerHTML = '';
    products.forEach(product => productsList.appendChild(product));
}

function getPriceValue(productElement) {
    const priceText = productElement.querySelector('.product-price').textContent;
    return parseFloat(priceText.replace(/[^\d.]/g, ''));
}

function getProductName(productElement) {
    return productElement.querySelector('.product-title').textContent;
}


// Filtrado de productos y manejo de subcategorías
document.querySelectorAll('.category_item').forEach(item => {
    item.addEventListener('click', function (e) {
        e.preventDefault();
        const category = this.getAttribute('category');
        const subcategoryList = document.getElementById(`${category}_subcategories`);

        // Ocultar todas las subcategorías
        document.querySelectorAll('.subcategory_list').forEach(subcategory => {
            if (subcategory !== subcategoryList) {
                subcategory.style.display = 'none';
            }
        });

        // Alternar la visibilidad de las subcategorías correspondientes
        if (subcategoryList) {
            subcategoryList.style.display = subcategoryList.style.display === 'block' ? 'none' : 'block';
        }

        // Filtrar productos por categoría
        document.querySelectorAll('.product-card').forEach(card => {
            const cardCategory = card.getAttribute('data-category');
            if (category === 'all' || cardCategory === category) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });

        // Actualizar la clase activa
        document.querySelectorAll('.category_item').forEach(i => i.classList.remove('ct_item-active'));
        this.classList.add('ct_item-active');
    });
});

document.querySelectorAll('.subcategory_item').forEach(item => {
    item.addEventListener('click', function (e) {
        e.preventDefault();
        // Obtener las categorías y subcategorías
        const category = this.getAttribute('category_item');
        const subcategory = this.getAttribute('subcategory_item');

        // Filtrar productos por subcategoría
        document.querySelectorAll('.product-card').forEach(card => {
            const cardCategory = card.getAttribute('data-category');
            const cardSubcategory = card.getAttribute('data-subcategory');
            // Mostrar solo los productos que coinciden con la categoría y subcategoría
            if (cardCategory === category && cardSubcategory === subcategory) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });

        // Actualizar la clase activa
        document.querySelectorAll('.subcategory_item').forEach(i => i.classList.remove('ct_item-active'));
        this.classList.add('ct_item-active');

        // Cerrar el modal de categorías después de seleccionar una subcategoría en móvil
        if (window.innerWidth <= 768) {
            const categoryModal = document.getElementById('category-filter-modal');
            categoryModal.classList.add('modal-slide-left');

            // Esperar a que termine la animación antes de ocultar el modal
            setTimeout(() => {
                categoryModal.style.display = 'none';
                categoryModal.classList.remove('modal-slide-left');
            }, 500); // Duración de la animación en milisegundos
        }
    });
});

// Funcionalidad para el modal de filtros
const filterIcon = document.querySelector('.filter-icon');
const categoryModal = document.getElementById('category-filter-modal');
const categoryCloseBtn = document.querySelector('.category-close');

filterIcon.addEventListener('click', () => {
    if (window.innerWidth <= 768) {
        categoryModal.style.display = 'block';
    }
});

categoryCloseBtn.addEventListener('click', () => {
    categoryModal.style.display = 'none';
});

window.addEventListener('click', (event) => {
    if (event.target == categoryModal) {
        categoryModal.style.display = 'none';
    }
});

// Asegurarse de que las categorías en el modal móvil funcionen igual que las categorías principales
document.querySelectorAll('.category_list_mobile .category_item').forEach(item => {
    item.addEventListener('click', function (e) {
        e.preventDefault();
        const category = this.getAttribute('category');

        document.querySelectorAll('.category_item').forEach(i => i.classList.remove('ct_item-active'));
        this.classList.add('ct_item-active');

        document.querySelectorAll('.product-card').forEach(card => {
            if (category === 'all' || card.dataset.category === category) {
                card.style.display = 'block';
            } else {
                card.style.display = 'none';
            }
        });

        // Mostrar subcategorías
        const subcategories = this.nextElementSibling;
        if (subcategories && subcategories.classList.contains('subcategory_list')) {
            subcategories.style.display = 'block';
        }

        // Cerrar el modal de categorías después de seleccionar una categoría en móvil
        if (window.innerWidth <= 768) {
            const categoryModal = document.getElementById('category-filter-modal');
            categoryModal.classList.add('modal-slide-left');

            // Esperar a que termine la animación antes de ocultar el modal
            setTimeout(() => {
                categoryModal.style.display = 'none';
                categoryModal.classList.remove('modal-slide-left');
            }, 500); // Duración de la animación en milisegundos
        }
    });
});