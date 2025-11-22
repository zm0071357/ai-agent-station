package ai.agent.station.trigger.http;

import ai.agent.station.api.UserService;
import ai.agent.station.api.dto.UserInfoResponseDTO;
import ai.agent.station.api.response.Response;
import ai.agent.station.domain.user.model.valobj.UserVO;
import ai.agent.station.domain.user.service.self.SelfService;
import ai.agent.station.types.enums.ResponseCodeEnum;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@SaCheckLogin
@CrossOrigin("*")
@RestController
@RequestMapping("/user")
public class UserController implements UserService {

    @Resource
    private SelfService selfService;

    @PostMapping("/info")
    @Override
    public Response<UserInfoResponseDTO> userInfo() {
        try {
            UserVO userVO = selfService.userInfo(StpUtil.getLoginIdAsString());
            return Response.<UserInfoResponseDTO>builder()
                    .code(ResponseCodeEnum.SUCCESS.getCode())
                    .info(ResponseCodeEnum.SUCCESS.getInfo())
                    .data(UserInfoResponseDTO.builder()
                            .userId(userVO.getUserId())
                            .userName(userVO.getUserName())
                            .userEmail(userVO.getEmail())
                            .executeCount(userVO.getExecuteCount())
                            .points(userVO.getPoints())
                            .build())
                    .build();
        } catch (SaTokenException e) {
            return Response.<UserInfoResponseDTO>builder()
                    .code(ResponseCodeEnum.LOGIN_TIMEOUT.getCode())
                    .info(ResponseCodeEnum.LOGIN_TIMEOUT.getInfo())
                    .build();
        } catch (Exception e) {
            return Response.<UserInfoResponseDTO>builder()
                    .code(ResponseCodeEnum.UN_ERROR.getCode())
                    .info(ResponseCodeEnum.UN_ERROR.getInfo())
                    .build();
        }
    }

}
