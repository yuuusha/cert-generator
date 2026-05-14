package com.diev;

import com.diev.gui.CertificateGeneratorFrame;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;

@SpringBootApplication
public class CertGeneratorApplication {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        var context = SpringApplication.run(CertGeneratorApplication.class, args);

        SwingUtilities.invokeLater(() -> {
            CertificateGeneratorFrame frame = context.getBean(CertificateGeneratorFrame.class);
            frame.init();
            frame.setVisible(true);
        });
    }
}