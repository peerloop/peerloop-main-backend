package com.peerloop.peerloopapp.domain.member.service;


import static com.peerloop.peerloopapp.global.exception.ErrorCode.DUPLICATED_EMAIL;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.NOT_FOUND_BY_EMAIL;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.NOT_FOUND_BY_ID;

import com.peerloop.peerloopapp.domain.auth.constant.OAuthProvider;
import com.peerloop.peerloopapp.domain.auth.dto.OAuthMemberInfo;
import com.peerloop.peerloopapp.domain.member.entity.Member;
import com.peerloop.peerloopapp.domain.member.repository.MemberRepository;
import com.peerloop.peerloopapp.global.common.enums.MemberRole;
import com.peerloop.peerloopapp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member getMemberById(String memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(NOT_FOUND_BY_ID));
    }

    public boolean existsById(String memberId) {
        return memberRepository.existsById(memberId);
    }

    public void createOAuthMember(OAuthMemberInfo oAuthMemberInfo, OAuthProvider oAuthProvider, MemberRole role) {
        String memberId = oAuthMemberInfo.getId();
        String email = oAuthMemberInfo.getEmail();
        Member member = Member.createMember(memberId, email, oAuthProvider, role);
        memberRepository.save(member);
    }

}
