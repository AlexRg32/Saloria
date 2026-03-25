package com.saloria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saloria.model.ServiceOffering;
import java.util.Optional;
import java.util.List;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
  List<ServiceOffering> findByEnterpriseId(Long enterpriseId);

  List<ServiceOffering> findByEnterpriseIdAndDeletedFalse(Long enterpriseId);

  List<ServiceOffering> findByEnterpriseIdInAndDeletedFalse(List<Long> enterpriseIds);

  Optional<ServiceOffering> findByIdAndEnterpriseIdAndDeletedFalse(Long id, Long enterpriseId);
}
