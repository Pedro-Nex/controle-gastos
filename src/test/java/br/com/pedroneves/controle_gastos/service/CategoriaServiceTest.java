package br.com.pedroneves.controle_gastos.service;

import br.com.pedroneves.controle_gastos.dto.CategoriaRequest;
import br.com.pedroneves.controle_gastos.dto.CategoriaResponse;
import br.com.pedroneves.controle_gastos.exception.ConflitoException;
import br.com.pedroneves.controle_gastos.model.Categoria;
import br.com.pedroneves.controle_gastos.repository.CategoriaRepository;
import br.com.pedroneves.controle_gastos.repository.TransacaoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoriaServiceTest {

    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final TransacaoRepository transacaoRepository = mock(TransacaoRepository.class);
    private final CategoriaService categoriaService =
            new CategoriaService(categoriaRepository, transacaoRepository);

    @Test
    void atualizaCategoriaExistente() {
        Categoria categoria = categoria(7L, "Lazer");
        when(categoriaRepository.findById(7L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Entretenimento", 7L)).thenReturn(false);
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoriaResponse response = categoriaService.atualizar(7L, new CategoriaRequest(" Entretenimento "));

        assertEquals(new CategoriaResponse(7L, "Entretenimento"), response);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void rejeitaNomeDuplicadoAoAtualizarCategoria() {
        Categoria categoria = categoria(7L, "Lazer");
        when(categoriaRepository.findById(7L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Transporte", 7L)).thenReturn(true);

        assertThrows(ConflitoException.class,
                () -> categoriaService.atualizar(7L, new CategoriaRequest("Transporte")));

        verify(categoriaRepository, never()).save(categoria);
    }

    @Test
    void listaCategoriasEmOrdemAlfabetica() {
        when(categoriaRepository.findAll(any(Sort.class))).thenReturn(java.util.List.of());

        categoriaService.listar();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(categoriaRepository).findAll(sortCaptor.capture());
        assertEquals(Sort.Direction.ASC, sortCaptor.getValue().getOrderFor("nome").getDirection());
    }

    private Categoria categoria(Long id, String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        return categoria;
    }
}
