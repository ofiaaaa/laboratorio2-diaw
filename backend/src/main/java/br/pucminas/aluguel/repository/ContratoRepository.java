package br.pucminas.aluguel.repository;

import br.pucminas.aluguel.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContratoRepository extends JpaRepository<Contrato, Long> {
}
