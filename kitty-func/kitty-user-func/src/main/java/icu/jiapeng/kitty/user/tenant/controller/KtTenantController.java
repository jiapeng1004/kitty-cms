/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.tenant.controller;

import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.api.tenant.api.TenantApi;
import icu.jiapeng.kitty.user.api.tenant.dto.TenantCreateDTO;
import icu.jiapeng.kitty.user.api.tenant.dto.TenantQueryPageDTO;
import icu.jiapeng.kitty.user.api.tenant.dto.TenantUpdateDTO;
import icu.jiapeng.kitty.user.tenant.service.KtTenantService;
import icu.jiapeng.kitty.user.api.tenant.vo.TenantVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class KtTenantController implements TenantApi {

    private final KtTenantService ktTenantService;

    @Override
    public String create(@Valid @RequestBody TenantCreateDTO dto) {
        return ktTenantService.create(dto);
    }

    @Override
    public List<TenantVO> list() {
        return ktTenantService.listAll();
    }

    @Override
    public PageRespVo<TenantVO> query(TenantQueryPageDTO query) {
        return ktTenantService.query(query);
    }

    @Override
    public TenantVO getById(@PathVariable String id) {
        return ktTenantService.getById(id);
    }

    @Override
    public boolean update(@PathVariable String id, @Valid @RequestBody TenantUpdateDTO dto) {
        return ktTenantService.update(id, dto);
    }

    @Override
    public boolean delete(@PathVariable String id) {
        return ktTenantService.delete(id);
    }
}