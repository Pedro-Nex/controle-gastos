package br.com.pedroneves.controle_gastos.repository;

import br.com.pedroneves.controle_gastos.model.Transacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    @EntityGraph(attributePaths = "categoria")
    List<Transacao> findAllByOrderByDataDescIdDesc();

    @EntityGraph(attributePaths = "categoria")
    List<Transacao> findByDataBetweenOrderByDataDescIdDesc(LocalDate inicio, LocalDate fim);

    boolean existsByCategoria_Id(Long categoriaId);
}
