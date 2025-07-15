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
package icu.jiapeng.kitty.user.auth.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.user.auth.entity.KtUserOauth2Client;
import icu.jiapeng.kitty.user.auth.mapper.KtUserOauth2ClientMapper;
import icu.jiapeng.kitty.user.auth.service.KtUserOauth2ClientService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class KtUserOauth2ClientServiceImpl extends ServiceImpl<KtUserOauth2ClientMapper, KtUserOauth2Client> implements KtUserOauth2ClientService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 客户端缓存key
     */
    private static final String CLIENT_CACHE_KEY = "oauth2:client:";

    /**
     * 客户端缓存过期时间(秒)
     */
    private static final long CLIENT_CACHE_EXPIRE = 36000;

    /**
     * 获取客户端
     *
     * @param clientId 客户端id
     * @return 客户端
     */
    @Override
    public KtUserOauth2Client getClientByClientId(String clientId) {
        return lambdaQuery().eq(KtUserOauth2Client::getClientId, clientId).last("LIMIT 1").one();
    }

    /**
     * 获取客户端
     *
     * @param clientId 客户端id
     * @return 客户端
     */
    @Override
    public KtUserOauth2Client getClientByClientIdWithCache(String clientId) {
        // 以上注释掉的代码 改成阻塞式代码
        String client = stringRedisTemplate.opsForValue().get(CLIENT_CACHE_KEY + clientId);
        KtUserOauth2Client ktUserOauth2Client = null;
        if (StringUtils.hasText(client)) {
            try {
                ktUserOauth2Client = JSONObject.parseObject(client, new TypeReference<>() {
                });
            } catch (Exception e) {
                log.error("客户端缓存解析错误", e);
            }
        }
        if (Objects.isNull(ktUserOauth2Client)) {
            ktUserOauth2Client = getClientByClientId(clientId);
            if (Objects.nonNull(ktUserOauth2Client)) {
                stringRedisTemplate.opsForValue().set(CLIENT_CACHE_KEY + clientId,
                        JSONObject.toJSONString(ktUserOauth2Client),
                        CLIENT_CACHE_EXPIRE, TimeUnit.SECONDS);
            }
        }
        return ktUserOauth2Client;
    }

}
