package icu.jiapeng.kitty.transcoder.func.auth;

import icu.jiapeng.kitty.transcoder.api.AccessKeyVO;
import icu.jiapeng.kitty.transcoder.api.CreateAccessKeyRequest;
import icu.jiapeng.kitty.transcoder.api.CreateAccessKeyResponse;

import java.util.List;
import java.util.Map;

public interface AuthService {

    /**
     * 生成 Access Key 和 Secret Key
     * @param request 创建请求（含 name）
     * @return 创建结果（accessKeyId、secretKey、name）
     */
    CreateAccessKeyResponse generateAccessKey(CreateAccessKeyRequest request);

    /**
     * 验证 Access Key 是否存在
     * @param accessKey Access Key
     * @return 是否存在
     */
    boolean validateAccessKey(String accessKey);

    /**
     * 获取 Secret Key
     * @param accessKey Access Key
     * @return Secret Key
     */
    String getSecretKey(String accessKey);

    /**
     * 生成签名
     * @param accessKey Access Key
     * @param secretKey Secret Key
     * @param params 请求参数
     * @param timestamp 时间戳
     * @return 签名
     */
    String generateSignature(String accessKey, String secretKey, Map<String, String> params, long timestamp);

    /**
     * 验证签名
     * @param accessKey Access Key
     * @param signature 签名
     * @param params 请求参数
     * @param timestamp 时间戳
     * @return 是否验证通过
     */
    boolean validateSignature(String accessKey, String signature, Map<String, String> params, long timestamp);

    /**
     * 生成登录 Token
     * @param accessKey Access Key
     * @return Token
     */
    String generateLoginToken(String accessKey);

    /**
     * 验证登录 Token
     * @param token Token
     * @return 是否验证通过
     */
    boolean validateLoginToken(String token);

    /**
     * 列出 Access Key（不含 secretKey）
     */
    List<AccessKeyVO> listAccessKeys();

    /**
     * 删除 Access Key（按 accessKeyId）
     * @param accessKeyId Access Key ID
     * @return 是否删除成功
     */
    boolean deleteAccessKey(String accessKeyId);
}