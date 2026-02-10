package icu.jiapeng.kitty.user.config;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import icu.jiapeng.kitty.user.scope.UserScoped;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 自定义公共字段自动注入
 *
 * @author xuyuxiang
 * @date 2020/3/31 15:42
 */
@Component
public class CustomMetaObjectHandler implements MetaObjectHandler {

    /**
     * 删除标志
     */
    private static final String DELETE_FLAG = "deleted";

    /**
     * 创建人
     */
    private static final String CREATE_USER = "creator";

    /**
     * 创建时间
     */
    private static final String CREATE_TIME = "createTime";

    /**
     * 更新人
     */
    private static final String UPDATE_USER = "updater";

    /**
     * 更新时间
     */
    private static final String UPDATE_TIME = "updateTime";

    @Override
    public void insertFill(MetaObject metaObject) {
        //为空则设置deleteFlag
        try {
            Object deleteFlag = metaObject.getValue(DELETE_FLAG);
            if (Objects.isNull(deleteFlag)) {
                setFieldValByName(DELETE_FLAG, 0, metaObject);
            }
        } catch (Exception _) {
        }
        try {
            //为空则设置createUser
            Object createUser = metaObject.getValue(CREATE_USER);
            if (Objects.isNull(createUser)) {
                setFieldValByName(CREATE_USER, this.getUserId(), metaObject);
            }
        } catch (Exception _) {
        }
        try {
            //为空则设置createTime
            Object createTime = metaObject.getValue(CREATE_TIME);
            if (Objects.isNull(createTime)) {
                setFieldValByName(CREATE_TIME, DateTime.now(), metaObject);
            }
        } catch (Exception _) {
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            //设置updateUser
            setFieldValByName(UPDATE_USER, this.getUserId(), metaObject);
        } catch (Exception _) {
        }
        try {
            //设置updateTime
            setFieldValByName(UPDATE_TIME, DateTime.now(), metaObject);
        } catch (Exception _) {
        }
    }

    /**
     * 获取用户id
     */
    private String getUserId() {
        String scopeUser = UserScoped.getUserId();
        if (StringUtils.hasText(scopeUser)) {
            return scopeUser;
        }
        try {
            String loginId = StpUtil.getLoginIdAsString();
            if (StringUtils.hasText(loginId)) {
                return loginId;
            }
        } catch (Exception _) {
        }
        return "system";
    }
}