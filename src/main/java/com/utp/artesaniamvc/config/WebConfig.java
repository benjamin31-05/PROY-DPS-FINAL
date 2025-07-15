package com.utp.artesaniamvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Configuración para recursos estáticos en classpath
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Alias para servir archivos desde /uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

        // Alias para servir imágenes desde /uploads/imagenes/
        registry.addResourceHandler("/imagenes/**")
                .addResourceLocations("file:uploads/imagenes/");

        // Alias para servir específicamente archivos desde /uploads/pagos/
        registry.addResourceHandler("/pagos/**")
                .addResourceLocations("file:uploads/pagos/");

        // Agrega más mappings según necesites
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");

        System.out.println("ResourceHandlers configurados");
    }
}
