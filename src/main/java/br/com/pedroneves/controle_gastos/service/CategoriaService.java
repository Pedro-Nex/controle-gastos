package br.com.pedroneves.controle_gastos.service;

import br.com.pedroneves.controle_gastos.dto.CategoriaRequest;
import br.com.pedroneves.controle_gastos.dto.CategoriaResponse;
import br.com.pedroneves.controle_gastos.exception.ConflitoException;
import br.com.pedroneves.controle_gastos.exception.RecursoNaoEncontradoException;
import br.com.pedroneves.controle_gastos.model.Categoria;
import br.com.pedroneves.controle_gastos.repository.CategoriaRepository;
import br.com.pedroneves.controle_gastos.repository.TransacaoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TransacaoRepository transacaoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            TransacaoRepository transacaoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll(Sort.by(Sort.Direction.ASC, "nome")).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return paraResponse(encontrarCategoria(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma categoria com esse nome.");
        }

        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = encontrarCategoria(id);
        String nome = request.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe uma categoria com esse nome.");
        }

        categoria.setNome(nome);
        return paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria de id " + id + " não foi encontrada."));

        if (transacaoRepository.existsByCategoria_Id(id)) {
            throw new ConflitoException("A categoria possui transações vinculadas e não pode ser excluída.");
        }
        categoriaRepository.delete(categoria);
    }

    private Categoria encontrarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria de id " + id + " não foi encontrada."));
    }

    private CategoriaResponse paraResponse(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome());
    }
}
