package com.peerloop.peerloopapp.domain.member.repository;

import com.peerloop.peerloopapp.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, String> {

//    Boolean existsByEmail(String email);
//
//    Optional<Member> findByEmail(String email);
}
