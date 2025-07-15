package com.utp.artesaniamvc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)

public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                // Recursos estáticos - asegúrate de incluir todas las rutas de recursos
                .requestMatchers(
                        "/css/**",
                        "/vendor/**",
                        "/js/**",
                        "/imagenes/**",
                        "/images/**",
                        "/img/**",
                        "/uploads/**",
                        "/assets/**",
                        "/recursos/**",
                        "/static/**",
                        "/webjars/**"
                ).permitAll()
                // Rutas públicas
                .requestMatchers(
                        "/",
                        "/artesaniamvc",
                        "/productoHome/**",
                        "/informacion",
                        "/index",
                        "/nosotros",
                        "/search",
                        "/error",
                        "/login",
                        "/registro",
                        "/verificar-codigo",
                        "/verificar",
                        "/registrar",
                        "/cart",
                        "/getCart",
                        "/delete/cart/**"
                ).permitAll()
                // Protección para la carpeta /uploads/pagos/
                .requestMatchers("/uploads/pagos/**").hasAnyRole("ADMIN", "OPEN", "UDEP")
                // URLs de administrador
                // URLs de administrador
                .requestMatchers("/admin/**", "/productos1/products", "/productos1/create", "/productos1/delete",
                        "/productos1/contacts", "/productos1/deleteAsunto"
                ).hasRole("ADMIN")
                // URLs específicas para clientes
                .requestMatchers("/cliente/**", "/cliente/home", "/contacto", "/contactoRegister", "/order",
                        "/saveOrder", "/productoHome/**", "/pedidos/**",
                        "/carrito/**").hasRole("CLIENTE")
                // Añadir URLs para roles OPEN (TIENDA1) y UDEP (TIENDA2)
                .requestMatchers("/tienda1/**").hasRole("TIENDA1")
                .requestMatchers("/tienda2/**").hasRole("TIENDA2")
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login").
                loginProcessingUrl("/login")
                .defaultSuccessUrl("/redirect", true)
                .permitAll()
                )
                .logout(logout -> logout.
                logoutSuccessUrl("/artesaniamvc")
                .permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
