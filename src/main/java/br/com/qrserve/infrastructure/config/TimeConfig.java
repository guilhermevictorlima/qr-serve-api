package br.com.qrserve.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }

    public LocalDate dataAtual() {
        return LocalDate.now(clock());
    }

    public LocalDateTime dataHoraAtual() {
        return LocalDateTime.now(clock());
    }

}
