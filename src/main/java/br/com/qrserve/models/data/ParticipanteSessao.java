package br.com.qrserve.models.data;

import jakarta.persistence.Column;
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
@Table(name = "participante_sessao")
public class ParticipanteSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sessao_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_participante_sessao_mesa"))
    private SessaoMesa sessao;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "nome", nullable = false, length = 255)
    private String nome;

    public ParticipanteSessao() {}

    public ParticipanteSessao(SessaoMesa sessao, String token, String nome) {
        this.sessao = sessao;
        this.token = token;
        this.nome = nome;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}

