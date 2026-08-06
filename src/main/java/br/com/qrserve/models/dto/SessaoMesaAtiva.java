package br.com.qrserve.models.dto;

import java.time.LocalDateTime;

public record SessaoMesaAtiva(
    Integer id,
    LocalDateTime dataHoraInicio
) {}
