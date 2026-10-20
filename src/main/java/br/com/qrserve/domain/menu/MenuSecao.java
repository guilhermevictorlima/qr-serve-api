package br.com.qrserve.domain.menu;

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
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_secao")
public class MenuSecao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(name = "posicao_ordenacao")
    private Integer posicaoOrdenacao;

    @OneToMany(
            mappedBy = "menuSecao",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MenuItem> itens = new ArrayList<>();

    public Integer getId() {
        return id;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Integer getPosicaoOrdenacao() {
        return posicaoOrdenacao;
    }

    public void setPosicaoOrdenacao(Integer posicaoOrdenacao) {
        this.posicaoOrdenacao = posicaoOrdenacao;
    }

    public List<MenuItem> getItens() {
        return itens;
    }

    public void setItens(List<MenuItem> itens) {
        this.itens = itens;
    }
}