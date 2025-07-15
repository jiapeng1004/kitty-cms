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
package icu.jiapeng.kitty.user.cfg.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.page.CommonOrder;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.cfg.dto.ClassCreateDTO;
import icu.jiapeng.kitty.user.cfg.dto.ConfigClassPageDTO;
import icu.jiapeng.kitty.user.cfg.entity.KtConfigClass;
import icu.jiapeng.kitty.user.cfg.mapper.KtConfigClassMapper;
import icu.jiapeng.kitty.user.cfg.service.KtConfigClassService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigClassListVo;
import org.springframework.stereotype.Service;

/**
 * 配置类服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Service
public class KtConfigClassServiceImpl extends ServiceImpl<KtConfigClassMapper, KtConfigClass> implements KtConfigClassService {
    @Override
    public PageRespVo<ConfigClassListVo> query(ConfigClassPageDTO query) {
        LambdaQueryChainWrapper<KtConfigClass> lm = lambdaQuery();
        if (StrUtil.isNotBlank(query.getOwner())) {
            lm.eq(KtConfigClass::getOwner, query.getOwner());
        }
        if (StrUtil.isNotBlank(query.getSearchKey())) {
            lm.like(KtConfigClass::getClassName, query.getSearchKey());
        }
        Page<KtConfigClass> page = new Page<>(query.getPage(), query.getSize());
        if (CollUtil.isNotEmpty(query.getOrders())) {
            query.getOrders().forEach(order -> page.addOrder(OrderItem.withExpression(StrUtil.toUnderlineCase(order.getOrderField()), CommonOrder.ASC.equals(order.getOrder()))));
        }
        Page<KtConfigClass> ktConfigPage = page(page, lm);
        return PageRespVo.<ConfigClassListVo>builder()
                .page(ktConfigPage.getCurrent())
                .size(ktConfigPage.getSize())
                .total(ktConfigPage.getTotal())
                .orders(query.getOrders())
                .records(ktConfigPage.getRecords().stream().map(BeansConvert.INSTANCE::class2ListVo).toList())
                .build();
    }

    @Override
    public String create(ClassCreateDTO classCreateDTO) {
        KtConfigClass ktConfigClass = BeanUtil.copyProperties(classCreateDTO, KtConfigClass.class);
        save(ktConfigClass);
        return ktConfigClass.getId();
    }
}
