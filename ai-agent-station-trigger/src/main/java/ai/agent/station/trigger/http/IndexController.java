package ai.agent.station.trigger.http;

import ai.agent.station.api.IndexService;
import ai.agent.station.api.dto.LoginRequestDTO;
import ai.agent.station.api.dto.RegisterRequestDTO;
import ai.agent.station.api.response.Response;
import ai.agent.station.domain.user.model.entity.LoginEntity;
import ai.agent.station.domain.user.model.entity.RegisterEntity;
import ai.agent.station.domain.user.service.login.LoginService;
import ai.agent.station.types.enums.ResponseCodeEnum;
import cn.dev33.satoken.annotation.SaCheckLogin;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/index")
public class IndexController implements IndexService {

    @Resource
    private LoginService loginService;

    @PostMapping("/login")
    @Override
    public Response<String> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            // 参数校验
            if (StringUtils.isBlank(loginRequestDTO.getUserId()) || StringUtils.isBlank(loginRequestDTO.getPassword()) ||
                    StringUtils.isBlank(loginRequestDTO.getEmail()) || StringUtils.isBlank(loginRequestDTO.getVerificationCode())) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 登录
            String token = loginService.login(LoginEntity.builder()
                            .userId(loginRequestDTO.getUserId())
                            .password(loginRequestDTO.getPassword())
                            .email(loginRequestDTO.getEmail())
                            .verificationCode(loginRequestDTO.getVerificationCode())
                    .build());
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data(token)
                    .build();
        } catch (Exception e) {
            log.error("登录失败：{}", e.getMessage());
            return Response.<String>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .data(e.getMessage())
                    .build();
        }
    }

    @PostMapping("/register")
    @Override
    public Response<String> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        try {
            // 参数校验
            if (StringUtils.isBlank(registerRequestDTO.getPassword()) || StringUtils.isBlank(registerRequestDTO.getEmail()) ||
                    StringUtils.isBlank(registerRequestDTO.getVerificationCode())) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 注册
            String token = loginService.register(RegisterEntity.builder()
                            .password(registerRequestDTO.getPassword())
                            .email(registerRequestDTO.getEmail())
                            .verificationCode(registerRequestDTO.getVerificationCode())
                    .build());
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data(token)
                    .build();
        } catch (Exception e) {
            log.error("注册失败：{}", e.getMessage());
            return Response.<String>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @PostMapping("/get_vc/{email}")
    @Override
    public Response<String> getVerificationCode(@PathVariable("email") String email) {
        try {
            // 参数校验
            if (StringUtils.isBlank(email)) {
                return Response.<String>builder()
                        .code(ResponseCodeEnum.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCodeEnum.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 获取验证码
            loginService.getVerificationCode(email);
            return Response.<String>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data("请到邮箱查看验证码")
                    .build();
        } catch (Exception e) {
            log.error("获取验证码失败：{}", e.getMessage());
            return Response.<String>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

    @SaCheckLogin
    @PostMapping("/logout")
    @Override
    public Response<String> logout() {
        StpUtil.logout();
        return Response.<String>builder()
                .code(ResponseCodeEnum.SUCCESS.getCode())
                .info(ResponseCodeEnum.SUCCESS.getInfo())
                .build();
    }

}
