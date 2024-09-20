package com.example.securedoc.repository;

import com.example.securedoc.entity.ConfirmationEntity;
import com.example.securedoc.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfirmationRepository extends JpaRepository<ConfirmationEntity, Long> {
    Optional<ConfirmationEntity> findByKey(String key);
    Optional<ConfirmationEntity> findByUser(UserEntity userEntity);
}
