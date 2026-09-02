package br.com.qrserve.infrastructure.time;

import br.com.qrserve.application.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class SystemTimeProvider implements TimeProvider {

    private final Clock clock;

    public SystemTimeProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDate dataAtual() {
        return LocalDate.now(clock);
    }

    public LocalDateTime dataHoraAtual() {
        return LocalDateTime.now(clock);
    }

}
