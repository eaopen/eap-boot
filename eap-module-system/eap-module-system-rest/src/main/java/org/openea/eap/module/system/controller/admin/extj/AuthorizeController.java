package org.openea.eap.module.system.controller.admin.extj;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.openea.eap.framework.common.pojo.CommonResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "操作权限", description = "Authorize")
@RestController
@RequestMapping("/permission/Authority")
public class AuthorizeController {

    /**
     * 获取模块列表展示字段
     *
     * @param moduleId 菜单Id
     * @return
     */
    @Operation(summary = "获取模块列表展示字段")
    @Parameters({
            @Parameter(name = "moduleId", description = "菜单id", required = true)
    })
    @GetMapping("/GetColumnsByModuleId/{moduleId}")
    public CommonResult<List> getColumnsByModuleId(@PathVariable("moduleId") String moduleId) {
        // todo
        return CommonResult.success(new ArrayList<>());
    }
}
