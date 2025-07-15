document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.container');
    const signInBtn = document.getElementById('btn-sign-in');
    const signUpBtn = document.getElementById('btn-sign-up');
    const signInButton = document.getElementById('signInButton');
    const signUpButton = document.getElementById('signUpButton');

  // Mantener la funcionalidad original de cambio de panel
    signInBtn.addEventListener('click', () => {
        container.classList.remove('toggle');
    });

    signUpBtn.addEventListener('click', () => {
        container.classList.add('toggle');
    });

    // Si hay un error o un registro exitoso, mantén el panel de registro abierto
    const errorMessage = document.querySelector('.alert-danger');
    const successMessage = document.querySelector('.alert-success');
    
    if (errorMessage || successMessage) {
        container.classList.add('toggle');
    }

    signUpButton.addEventListener('click', () => {
        const name = document.getElementById('signUpName').value;
        const email = document.getElementById('signUpEmail').value;
        const password = document.getElementById('signUpPassword').value;

        if (name && email && password) {
            localStorage.setItem(email, JSON.stringify({name, password}));
            alert('Registro exitoso. Ahora puede iniciar sesiÃ³n.');
            container.classList.remove('toggle');
        } else {
            alert('Por favor complete todos los campos.');
        }
    });

    signInButton.addEventListener('click', () => {
        const email = document.getElementById('signInEmail').value;
        const password = document.getElementById('signInPassword').value;

        const user = JSON.parse(localStorage.getItem(email));

        if (user && user.password === password) {
            alert(`Bienvenido, ${user.name}`);
        } else {
            alert('Correo o contraseÃ±a incorrectos.');
        }
    });
    
});


document.addEventListener('DOMContentLoaded', () => {
  // Selecciona todos los iconos de ojo y campos de contraseña
    const passwordInputs = document.querySelectorAll('input[type="password"]');
    const togglePasswordIcons = document.querySelectorAll('#togglePassword');

    // Itera sobre cada icono de ojo y su campo de contraseña correspondiente
    togglePasswordIcons.forEach((icon, index) => {
        icon.addEventListener('click', () => {
            const passwordInput = passwordInputs[index];
            
            // Cambia el tipo de entrada entre "password" y "text"
            if (passwordInput.type === "password") {
                passwordInput.type = "text";
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                passwordInput.type = "password";
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });

});
