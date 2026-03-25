package com.saloria.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.saloria.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
  List<User> findByEnterpriseIdAndArchivedFalse(Long enterpriseId);

  List<User> findByEnterpriseIdAndRoleAndArchivedFalse(Long enterpriseId, com.saloria.model.Role role);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailAndArchivedFalse(String email);

  Optional<User> findByIdAndArchivedFalse(Long id);

  Optional<User> findByIdAndEnterpriseIdAndArchivedFalse(Long id, Long enterpriseId);
}
