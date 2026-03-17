package icu.jiapeng.kitty.user.cfg.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.autoconfigure.IdentifierGeneratorAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusLanguageDriverAutoConfiguration;
import icu.jiapeng.kitty.common.core.page.CommonOrder;
import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.dto.ConfigQueryPageDTO;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigListVo;
import icu.jiapeng.kitty.user.config.UserFuncConfig;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(classes = KtConfigServiceImplTest.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import({
        SpringUtil.class
        , DataSourceAutoConfiguration.class
        , MybatisPlusInnerInterceptorAutoConfiguration.class
        , IdentifierGeneratorAutoConfiguration.class
        , MybatisPlusLanguageDriverAutoConfiguration.class
        , MybatisPlusAutoConfiguration.class
        , KtConfigServiceImpl.class
        , UserFuncConfig.class
})
@ActiveProfiles("loc-k3s")
class KtConfigServiceImplTest {
    @Resource
    private KtConfigService ktConfigService;

    @Test
    void pageOrder() {
        // 时间正序
        ConfigQueryPageDTO query = new ConfigQueryPageDTO();
        query.setOrders(List.of(new PageReqDTO.OrderItem("createTime", CommonOrder.ASC)));
        query.setPage(1L);
        query.setSize(10L);
        PageRespVo<ConfigListVo> pageResult = ktConfigService.query(query);
        List<ConfigListVo> records = pageResult.getRecords();
        // 紧邻元素遍历法
        ConfigListVo ignored = records.stream().reduce(null, (pre, cur) -> {
            if (ObjectUtil.isAllNotEmpty(pre, cur)) {
                // 比较cur的日期是不是比前的日期大
                if (DateUtil.compare(cur.getCreateTime(), pre.getCreateTime()) < 0) {
                    // 后面的不能更大
                    fail("时间不能降序");
                }
            }
            return cur;
        });
        // 时间倒序
        query.setOrders(List.of(new PageReqDTO.OrderItem("createTime", CommonOrder.DESC)));
        pageResult = ktConfigService.query(query);
        records = pageResult.getRecords();
        ignored = records.stream().reduce(null, (pre, cur) -> {
            if (ObjectUtil.isAllNotEmpty(pre, cur)) {
                // 比较cur的日期是不是比前的日期小
                if (DateUtil.compare(cur.getCreateTime(), pre.getCreateTime()) > 0) {
                    // 后面的不能更小
                    fail("时间不能升序");
                }
            }
            return cur;
        });

        // 复合排序时间倒叙 id正序
        query.setOrders(List.of(new PageReqDTO.OrderItem("createTime", CommonOrder.DESC), new PageReqDTO.OrderItem("id", CommonOrder.ASC)));
        pageResult = ktConfigService.query(query);
        records = pageResult.getRecords();
        ignored = records.stream().reduce(null, (pre, cur) -> {
            if (ObjectUtil.isAllNotEmpty(pre, cur)) {
                // 比较cur的日期是不是比前的日期小
                if (DateUtil.compare(cur.getCreateTime(), pre.getCreateTime()) > 0) {
                    // 后面的不能更小
                    fail("时间不能升序");
                }
                // 如果时间相等
                if (DateUtil.compare(cur.getCreateTime(), pre.getCreateTime()) == 0) {
                    // 比较id是不是比前的id小
                    if (cur.getId().compareTo(pre.getId()) < 0) {
                        // 后面的不能更小
                        fail("id不能降序");
                    }
                }
            }
            return cur;
        });

        //复合排序时间倒序 id倒序
        query.setOrders(List.of(new PageReqDTO.OrderItem("createTime", CommonOrder.DESC), new PageReqDTO.OrderItem("id", CommonOrder.DESC)));
        pageResult = ktConfigService.query(query);
        records = pageResult.getRecords();
        ignored = records.stream().reduce(null, (pre, cur) -> {
            if (ObjectUtil.isAllNotEmpty(pre, cur)) {
                // 比较cur的日期是不是比前的日期小
                if (DateUtil.compare(cur.getCreateTime(), pre.getCreateTime()) > 0) {
                    // 后面的不能更小
                    fail("时间不能升序");
                }
                // 如果时间相等
                if (DateUtil.compare(cur.getCreateTime(), pre.getCreateTime()) == 0) {
                    // 比较id是不是比前的id小
                    if (cur.getId().compareTo(pre.getId()) > 0) {
                        // 后面的不能更小
                        fail("id不能升序");
                    }
                }
            }
            return cur;
        });
    }



}