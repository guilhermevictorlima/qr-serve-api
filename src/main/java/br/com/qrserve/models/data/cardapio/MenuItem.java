package br.com.qrserve.models.data.cardapio;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_item")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_secao_id", nullable = false)
    private MenuSecao menuSecao;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "posicao_ordenacao")
    private Integer posicaoOrdenacao;

    @OneToMany(
            mappedBy = "menuItem",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ImagemMenuItem> imagens = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public MenuSecao getMenuSecao() {
        return menuSecao;
    }

    public void setMenuSecao(MenuSecao menuSecao) {
        this.menuSecao = menuSecao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getPosicaoOrdenacao() {
        return posicaoOrdenacao;
    }

    public void setPosicaoOrdenacao(Integer posicaoOrdenacao) {
        this.posicaoOrdenacao = posicaoOrdenacao;
    }

    public List<ImagemMenuItem> getImagens() {
        return imagens;
    }

    public void setImagens(List<ImagemMenuItem> imagens) {
        this.imagens = imagens;
    }
}