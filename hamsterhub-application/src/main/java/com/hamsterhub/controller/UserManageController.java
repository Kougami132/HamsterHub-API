package com.hamsterhub.controller;

import com.hamsterhub.annotation.Token;
import com.hamsterhub.service.config.SystemConfig;
import com.hamsterhub.convert.UserConvert;
import com.hamsterhub.response.Response;
import com.hamsterhub.response.UserResponse;
import com.hamsterhub.database.dto.UserDTO;
import com.hamsterhub.database.service.UserService;
import com.hamsterhub.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Tag(name = "用户管理 数据接口")
@RequestMapping("api")
public class UserManageController {

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    private UserService userService;


    @Operation(summary ="新建账号")
    @PostMapping(value = "/addUser")
    @Token("0")
    public Response add(@RequestBody UserVO userVO) {
        UserDTO userDTO = UserConvert.INSTANCE.vo2dto(userVO);
        userDTO.setUsername(userDTO.getUsername().toLowerCase()); // 统一小写
        userDTO.setPassModified();
        userDTO.setPassword("AAA123456"); // 默认密码
        userService.create(userDTO);
        return Response.success().msg("新建成功");
    }

    @Operation(summary ="删除账号")
    @PostMapping(value = "/delUser")
    @Token("0")
    public Response del(@RequestParam("id") Long id) {
        userService.delete(id);
        return Response.success().msg("删除账号");
    }

    @Operation(summary ="更新账号")
    @PostMapping(value = "/updateUser")
    @Token("0")
    public Response update(@RequestBody UserVO userVO) {
        userVO.setUsername(userVO.getUsername().toLowerCase());

        UserDTO userDTO = UserConvert.INSTANCE.vo2dto(userVO);
        userDTO.setPassModified();
        userService.updateForAdmin(userDTO);
        return Response.success().msg("更新成功");
    }

    @Operation(summary ="查询所有账号")
    @GetMapping(value = "/fetchUserAll")
    @Token("0")
    public Response fetchAll() {
        List<UserDTO> userDTOS = userService.FetchAll();
        List<UserResponse> userResponses = UserConvert.INSTANCE.dto2resBatch(userDTOS);
        return Response.success().data(userResponses);
    }


}
