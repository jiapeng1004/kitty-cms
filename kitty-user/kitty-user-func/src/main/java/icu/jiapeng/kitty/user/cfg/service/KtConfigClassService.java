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
package icu.jiapeng.kitty.user.cfg.service;


import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.dto.ClassCreateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ClassUpdateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ConfigClassPageDTO;
import icu.jiapeng.kitty.user.cfg.entity.KtConfigClass;
import icu.jiapeng.kitty.user.cfg.vo.ConfigClassListVo;

/**
 * 配置分类服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
public interface KtConfigClassService extends IService<KtConfigClass> {
    /**
     * 查询
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageRespVo<ConfigClassListVo> query(ConfigClassPageDTO query);

    /**
     * 新增
     *
     * @param classCreateDTO 新增参数
     * @return 新增结果
     */
    String create(ClassCreateDTO classCreateDTO);

    /**
     * 更新
     *
     * @param id  分类 id
     * @param dto 更新参数
     * @return 是否成功
     */
    boolean update(String id, ClassUpdateDTO dto);
}
