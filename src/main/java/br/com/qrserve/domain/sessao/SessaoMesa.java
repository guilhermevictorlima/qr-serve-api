package br.com.qrserve.domain.sessao;

import java.time.LocalDateTime;

import br.com.qrserve.domain.mesa.Mesa;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessao_mesa")
public class SessaoMesa {
    private static final int LIMITE_SESSAO_ABERTA_EM_MINUTOS = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sessao_mesa_mesa"))
    private Mesa mesa;

    @Embedded
    private TokenSessao token;

    @Column(name = "data_hora_inicio", nullable = false)
    private LocalDateTime dataHoraInicio;

    @Column(name = "data_hora_encerramento")
    private LocalDateTime dataHoraEncerramento;

    public SessaoMesa() {}

    public SessaoMesa(Mesa mesa, TokenSessao token, LocalDateTime dataHoraInicio) {
        this.mesa = mesa;
        this.token = token;
        this.dataHoraInicio = dataHoraInicio;
    }

    public static SessaoMesa create(Integer mesaId, LocalDateTime dataHoraAtual) {
        return new SessaoMesa(new Mesa(mesaId), TokenSessao.create(mesaId, dataHoraAtual), dataHoraAtual);
    }

    public SessaoMesa(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Mesa getMesa() {
        return mesa;
    }

    public void setMesa(Mesa mesa) {
        this.mesa = mesa;
    }

    public TokenSessao getToken() {
        return token;
    }

    public void setToken(TokenSessao token) {
        this.token = token;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public LocalDateTime getDataHoraEncerramento() {
        return dataHoraEncerramento;
    }

    public void setDataHoraEncerramento(LocalDateTime dataHoraEncerramento) {
        this.dataHoraEncerramento = dataHoraEncerramento;
    }

    public boolean isPermitidoEntrarNaSessao(LocalDateTime dataHoraAtual) {
        LocalDateTime dataHoraLimite = dataHoraInicio.plusMinutes(LIMITE_SESSAO_ABERTA_EM_MINUTOS);
        return !dataHoraAtual.isAfter(dataHoraLimite);
    }
}
