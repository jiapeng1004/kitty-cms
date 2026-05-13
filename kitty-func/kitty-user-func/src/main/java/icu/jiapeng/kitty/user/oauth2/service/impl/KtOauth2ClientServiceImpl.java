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
package icu.jiapeng.kitty.user.oauth2.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.api.oauth2.dto.Oauth2ClientCreateDTO;
import icu.jiapeng.kitty.user.api.oauth2.dto.Oauth2ClientQueryPageDTO;
import icu.jiapeng.kitty.user.api.oauth2.dto.Oauth2ClientUpdateDTO;
import icu.jiapeng.kitty.user.oauth2.entity.KtOauth2Client;
import icu.jiapeng.kitty.user.oauth2.mapper.KtOauth2ClientMapper;
import icu.jiapeng.kitty.user.oauth2.service.KtOauth2ClientService;
import icu.jiapeng.kitty.user.api.oauth2.vo.Oauth2ClientVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KtOauth2ClientServiceImpl extends ServiceImpl<KtOauth2ClientMapper, KtOauth2Client> implements KtOauth2ClientService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

    @Override
    public KtOauth2Client getClientByClientId(String clientId) {
        return lambdaQuery().eq(KtOauth2Client::getClientId, clientId).last("LIMIT 1").one();
    }

    @Override
    public KtOauth2Client getClientByClientIdWithCache(String clientId) {
        String client = stringRedisTemplate.opsForValue().get(CLIENT_CACHE_KEY + clientId);
        KtOauth2Client ktOauth2Client = null;
        if (StringUtils.hasText(client)) {
            try {
                ktOauth2Client = JSONObject.parseObject(client, new TypeReference<>() {
                });
            } catch (Exception e) {
                log.error("客户端缓存解析错误", e);
            }
        }
        if (Objects.isNull(ktOauth2Client)) {
            ktOauth2Client = getClientByClientId(clientId);
            if (Objects.nonNull(ktOauth2Client)) {
                stringRedisTemplate.opsForValue().set(CLIENT_CACHE_KEY + clientId,
                        JSONObject.toJSONString(ktOauth2Client),
                        CLIENT_CACHE_EXPIRE, TimeUnit.SECONDS);
            }
        }
        return ktOauth2Client;
    }

    @Override
    public PageRespVo<Oauth2ClientVO> query(Oauth2ClientQueryPageDTO query) {
        LambdaQueryWrapper<KtOauth2Client> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getSearchKey())) {
            wrapper.and(w -> w.like(KtOauth2Client::getClientName, query.getSearchKey())
                    .or()
                    .like(KtOauth2Client::getClientId, query.getSearchKey()));
        }
        wrapper.orderByDesc(KtOauth2Client::getCreateTime);
        Page<KtOauth2Client> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageRespVo.<Oauth2ClientVO>builder()
                .page(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .orders(query.getOrders())
                .records(page.getRecords().stream().map(this::toVo).toList())
                .build();
    }

    @Override
    public Oauth2ClientVO getDetail(String id) {
        KtOauth2Client entity = getById(id);
        return entity == null ? null : toVo(entity);
    }

    @Override
    public Oauth2ClientVO create(Oauth2ClientCreateDTO dto) {
        KtOauth2Client entity = BeanUtil.copyProperties(dto, KtOauth2Client.class);
        fillCreateDefaults(entity);
        DuplicateKeyException e = null;
        for (int i = 0; i < 2; i++) {
            try {
                String clientId = generateClientId();
                entity.setId(clientId);
                entity.setClientId(clientId);
                entity.setClientSecret(generateClientSecret());
                save(entity);
                return toVo(entity);
            } catch (DuplicateKeyException duplicateKeyException) {
                log.warn("OAuth2客户端创建失败，已重试：{}", entity.getId());
                e = duplicateKeyException;
            }
        }
        throw new IllegalStateException("OAuth2客户端创建失败", e);
    }

    @Override
    public boolean update(String id, Oauth2ClientUpdateDTO dto) {
        KtOauth2Client entity = getById(id);
        if (entity == null) {
            return false;
        }
        BeanUtil.copyProperties(dto, entity);
        entity.setId(id);
        boolean updated = updateById(entity);
        if (updated) {
            evictClientCache(entity.getClientId());
        }
        return updated;
    }

    @Override
    public boolean delete(String id) {
        KtOauth2Client entity = getById(id);
        boolean deleted = removeById(id);
        if (deleted && entity != null) {
            evictClientCache(entity.getClientId());
        }
        return deleted;
    }

    private Oauth2ClientVO toVo(KtOauth2Client entity) {
        return BeanUtil.copyProperties(entity, Oauth2ClientVO.class);
    }

    private void fillCreateDefaults(KtOauth2Client entity) {
        if (entity.getAccessTokenTimeout() == null) {
            entity.setAccessTokenTimeout(7200L);
        }
        if (entity.getRefreshTokenTimeout() == null) {
            entity.setRefreshTokenTimeout(2592000L);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getRequireAuthorizationConsent() == null) {
            entity.setRequireAuthorizationConsent(0);
        }
    }

    private String generateClientId() {
        return randomUrlSafeString(18);
    }

    private String generateClientSecret() {
        return randomUrlSafeString(36);
    }

    private String randomUrlSafeString(int byteLength) {
        byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void evictClientCache(String clientId) {
        if (StrUtil.isNotBlank(clientId)) {
            stringRedisTemplate.delete(CLIENT_CACHE_KEY + clientId);
        }
    }
}
