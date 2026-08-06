package br.com.qrserve.models.cardapio;

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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@Table(name = "item_cardapio_imagem")
public class ItemCardapioImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_cardapio_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_cardapio_imagem_item_cardapio"))
    private ItemCardapio itemCardapio;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "ordem_exibicao", nullable = false)
    private Integer ordemExibicao = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ItemCardapio getItemCardapio() {
        return itemCardapio;
    }

    public void setItemCardapio(ItemCardapio itemCardapio) {
        this.itemCardapio = itemCardapio;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getOrdemExibicao() {
        return ordemExibicao;
    }

    public void setOrdemExibicao(Integer ordemExibicao) {
        this.ordemExibicao = ordemExibicao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemCardapioImagem that = (ItemCardapioImagem) o;

        return new EqualsBuilder()
                .append(getItemCardapio(), that.getItemCardapio())
                .append(getUrl(), that.getUrl())
                .append(getOrdemExibicao(), that.getOrdemExibicao())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getItemCardapio())
                .append(getUrl())
                .append(getOrdemExibicao())
                .toHashCode();
    }
}
