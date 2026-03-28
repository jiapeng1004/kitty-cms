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
package icu.jiapeng.kitty.plugin.sa.config;

import cn.dev33.satoken.dao.SaTokenDao;
import icu.jiapeng.kitty.user.auth.grpc.*;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;


@AllArgsConstructor
public class SaTokenDaoKittyUser implements SaTokenDao {

    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    public String get(String key) {
        SaTokenDaoGetResp saTokenDaoGetResp = authServiceBlockingStub.saTokenDaoGet(SaTokenDaoGetReq.newBuilder()
                .setKey(key)
                .build());
        return StringUtils.hasText(saTokenDaoGetResp.getValue()) ? saTokenDaoGetResp.getValue() : null;
    }

    public void set(String key, String value, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void update(String key, String value) {
        SaTokenDaoUpdateResp _ = authServiceBlockingStub.saTokenDaoUpdate(SaTokenDaoUpdateReq.newBuilder()
                .setKey(key)
                .setValue(value)
                .build());
    }

    public void delete(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getTimeout(String key) {
        SaTokenDaoTimeOutResp saTokenDaoTimeOutResp = authServiceBlockingStub.saTokenDaoTimeOut(SaTokenDaoTimeOutReq.newBuilder()
                .setKey(key)
                .build());
        return saTokenDaoTimeOutResp.getTimeOut();
    }

    public void updateTimeout(String key, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getObject(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setObject(String key, Object object, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateObject(String key, Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void deleteObject(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getObjectTimeout(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateObjectTimeout(String key, long timeout) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}