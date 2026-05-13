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
package icu.jiapeng.kitty.plugin.sa.openfeign;

import cn.dev33.satoken.dao.SaTokenDao;
import icu.jiapeng.kitty.user.api.internal.api.UserInternalAuthApi;
import icu.jiapeng.kitty.user.api.internal.dto.SaTokenDaoUpdateBody;
import icu.jiapeng.kitty.user.api.internal.dto.SaTokenValuePayload;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;

@AllArgsConstructor
public class SaTokenDaoKittyUserOpenFeign implements SaTokenDao {

    private final UserInternalAuthApi userInternalAuthApi;

    @Override
    public String get(String key) {
        SaTokenValuePayload payload = userInternalAuthApi.saTokenDaoGet(key);
        if (payload == null || !StringUtils.hasText(payload.value())) {
            return null;
        }
        return payload.value();
    }

    @Override
    public void set(String key, String value, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(String key, String value) {
        userInternalAuthApi.saTokenDaoUpdate(new SaTokenDaoUpdateBody(key, value));
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getTimeout(String key) {
        var payload = userInternalAuthApi.saTokenDaoTimeout(key);
        return payload == null ? 0 : payload.timeout();
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object getObject(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateObject(String key, Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteObject(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getObjectTimeout(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateObjectTimeout(String key, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
