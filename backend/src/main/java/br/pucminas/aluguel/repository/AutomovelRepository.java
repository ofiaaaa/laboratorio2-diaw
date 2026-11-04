package br.pucminas.aluguel.repository;

import br.pucminas.aluguel.model.Automovel;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface AutomovelRepository extends JpaRepository<Automovel, Long> {

    List<Automovel> findAllByOrderByMarcaAscModeloAsc();

    @Query("""
            select a from Automovel a join a.proprietario p
            where type(p) <> Cliente
            order by a.marca, a.modelo
            """)
    List<Automovel> findDisponiveisParaAluguel();

    /** Serializa pedidos concorrentes sobre o mesmo automóvel durante a checagem de período. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Automovel a where a.id = :id")
    Optional<Automovel> findByIdParaAtualizar(Long id);

    boolean existsByPlaca(String placa);

    boolean existsByMatricula(String matricula);

    boolean existsByProprietarioId(Long proprietarioId);
}
