package com.utp.artesaniamvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCorreoRespuesta(String email, String nombre, String asunto, String respuesta) {
        // Crear el mensaje
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("tu_correo@gmail.com"); // Correo del remitente
        message.setTo(email);  // Correo del destinatario
        message.setSubject("Respuesta a tu consulta: " + asunto);  // Asunto del correo
        message.setText("Hola " + nombre + ",\n\n" +
                "Hemos respondido a tu consulta sobre: " + asunto + ".\n\n" +
                "Respuesta: " + respuesta + "\n\n" +
                "Gracias por contactarnos.\n" +
                "Saludos,\nHeladeria Breeze");  // Cuerpo del correo

        // Enviar el correo
        mailSender.send(message);
    }
}
