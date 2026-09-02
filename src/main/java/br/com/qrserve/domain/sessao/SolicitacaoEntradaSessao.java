package br.com.qrserve.domain.sessao;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_entrada_sessao")
public class SolicitacaoEntradaSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participante_sessao_mesa"))
    private SessaoMesa sessao;

    @Embedded
    private TokenUsuario token;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "data_hora_solicitacao", nullable = false)
    private LocalDateTime dataHoraSolicitacao;

    public SolicitacaoEntradaSessao() {}

    public SolicitacaoEntradaSessao(SessaoMesa sessao, TokenUsuario token, String nome, LocalDateTime dataHoraSolicitacao) {
        this.sessao = sessao;
        this.token = token;
        this.nome = nome;
        this.dataHoraSolicitacao = dataHoraSolicitacao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public SessaoMesa getSessao() {
        return sessao;
    }

    public void setSessao(SessaoMesa sessao) {
        this.sessao = sessao;
    }

    public TokenUsuario getToken() {
        return token;
    }

    public void setToken(TokenUsuario token) {
        this.token = token;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDateTime getDataHoraSolicitacao() {
        return dataHoraSolicitacao;
    }

    public void setDataHoraSolicitacao(LocalDateTime dataHoraSolicitacao) {
        this.dataHoraSolicitacao = dataHoraSolicitacao;
    }
}
