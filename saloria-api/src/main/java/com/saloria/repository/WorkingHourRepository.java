package com.saloria.repository;

import com.saloria.model.WorkingHour;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WorkingHourRepository extends JpaRepository<WorkingHour, Long> {
  List<WorkingHour> findByEnterpriseIdAndUserIdIsNull(Long enterpriseId);

  List<WorkingHour> findByUser_Id(Long userId);

  List<WorkingHour> findByEnterpriseId(Long enterpriseId);

  Optional<WorkingHour> findFirstByUser_IdAndDay(Long userId, String day);

  Optional<WorkingHour> findFirstByEnterpriseIdAndUserIdIsNullAndDay(Long enterpriseId, String day);
}
