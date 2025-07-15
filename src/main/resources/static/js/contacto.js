document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    form.addEventListener('submit', function(event) {
        let isValid = true;
        
        // Validar nombre
        const name = document.getElementById('name');
        if (name.value.trim() === '') {
            isValid = false;
            name.classList.add('is-invalid');
        } else {
            name.classList.remove('is-invalid');
        }
        
        // Validar email
        const email = document.getElementById('email');
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(email.value)) {
            isValid = false;
            email.classList.add('is-invalid');
        } else {
            email.classList.remove('is-invalid');
        }
        
        // Validar mensaje
        const message = document.getElementById('message');
        if (message.value.trim() === '') {
            isValid = false;
            message.classList.add('is-invalid');
        } else {
            message.classList.remove('is-invalid');
        }
        
        if (!isValid) {
            event.preventDefault();
        }
    });
});
