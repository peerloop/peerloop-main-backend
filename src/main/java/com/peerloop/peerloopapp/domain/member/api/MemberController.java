package com.peerloop.peerloopapp.domain.member.api;

import com.peerloop.peerloopapp.domain.member.api.request.SignUpRequest;
import com.peerloop.peerloopapp.domain.member.api.response.SignUpResponse;
import com.peerloop.peerloopapp.domain.member.facade.MemberFacade;
import com.peerloop.peerloopapp.global.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "01. Member Controller", description = "회원 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MemberController {

    private final MemberFacade memberFacade;

    // TODO: swagger에 자동으로 response status 적용되게 하려면 @ResponseStatus를 추가로 사용해야 할 것 같음
//    @Operation(summary = "회원가입", description = "email과 password를 이용하여 회원가입을 진행합니다.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201")
//    })
//    @PostMapping("/sign-up")
//    public ResponseEntity<SuccessResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
//        SignUpResponse response = memberFacade.signUp(request);
//        // TODO: ResponseEntity.created(uri) 형태로 변경
//        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessResponse<>(response));
//    }
}
