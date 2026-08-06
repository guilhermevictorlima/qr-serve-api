package com.rochacore.qrserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QrServeApplication {

    static void main(String[] args) {
        SpringApplication.run(QrServeApplication.class, args);
        System.out.println("Teste");
    }

}
