package br.com.qrserve.application.time;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDate dataAtual();
    LocalDateTime dataHoraAtual();
}
