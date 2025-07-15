package com.utp.artesaniamvc.service;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationCodeService {

    @Autowired
    private JavaMailSender mailSender;
    
    public String generarCodigoVerificacion() {
        // Genera un código de 6 dígitos
        return String.format("%06d", new Random().nextInt(999999));
    }
    
    public void enviarCodigoVerificacion(String email, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Código de Verificación");
        mensaje.setText("Su código de verificación es: " + codigo);
        
        mailSender.send(mensaje);
    }
}