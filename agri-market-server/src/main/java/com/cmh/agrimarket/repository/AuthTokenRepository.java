package com.cmh.agrimarket.repository;

import com.cmh.agrimarket.entity.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long> {
    Optional<AuthToken> findByToken(String token);

    @Modifying
    @Query("delete from AuthToken t where t.token = :token")
    void deleteByToken(@Param("token") String token);

    @Modifying
    @Query("delete from AuthToken t where t.userId = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
