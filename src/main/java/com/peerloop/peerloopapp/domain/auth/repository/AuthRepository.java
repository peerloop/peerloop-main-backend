package com.peerloop.peerloopapp.domain.auth.repository;


import com.peerloop.peerloopapp.domain.auth.entity.Auth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByRefreshToken(String refreshToken);

    Optional<Auth> findByMemberId(String memberId);

}
