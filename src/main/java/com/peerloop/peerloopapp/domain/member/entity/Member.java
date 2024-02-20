package com.peerloop.peerloopapp.domain.member.entity;

import com.peerloop.peerloopapp.domain.auth.constant.OAuthProvider;
import com.peerloop.peerloopapp.global.common.domain.BaseTimeEntity;
import com.peerloop.peerloopapp.global.common.enums.MemberRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.util.StringUtils;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseTimeEntity {

    @Id
    @Column(name = "member_id")
    private String id;

    @Column(name = "email")
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OAuthProvider oAuthProvider;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    // -- Construction Logic

    // Create OAuth Member (In OAuth flow, detailed member info such as bio is NOT provided)
    public static Member createMember(String id, String email, OAuthProvider oAuthProvider, MemberRole role) {
        return Member.builder()
                .id(id)
                .email(email)
                .oAuthProvider(oAuthProvider)
                .role(role)
                .build();
    }

}
