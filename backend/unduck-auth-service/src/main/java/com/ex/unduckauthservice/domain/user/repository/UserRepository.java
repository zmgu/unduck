package com.ex.unduckauthservice.domain.user.repository;

import com.ex.unduckauthservice.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
