package br.pucminas.aluguel.repository;

import br.pucminas.aluguel.model.PedidoAluguel;
import br.pucminas.aluguel.model.StatusPedido;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PedidoAluguelRepository extends JpaRepository<PedidoAluguel, Long> {

    List<PedidoAluguel> findAllByOrderByCriadoEmDesc();

    List<PedidoAluguel> findByClienteIdOrderByCriadoEmDesc(Long clienteId);

    boolean existsByClienteId(Long clienteId);

    boolean existsByAutomovelIdAndStatusInAndIdNot(Long automovelId, Collection<StatusPedido> status, Long id);

    @Query("""
            select count(p) > 0 from PedidoAluguel p
            where p.automovel.id = :automovelId
              and p.id <> :ignorarPedidoId
              and p.status in :status
              and p.dataInicio <= :fim
              and p.dataFim >= :inicio
            """)
    boolean existeConflitoDePeriodo(Long automovelId, Long ignorarPedidoId, Collection<StatusPedido> status,
                                    LocalDate inicio, LocalDate fim);
}
