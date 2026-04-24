package icu.jiapeng.kitty.material.behavior;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import icu.jiapeng.kitty.material.config.MamConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * 向 kitty-data（或兼容端点）转发行为事件；未配置 dataEventBaseUrl 时仅打日志。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialDataEventClient {

    private static final String EVENT_MAM_DOWNLOAD = "mam.resource.download";

    private final MamConfigProperties mamConfigProperties;
    private final RestTemplate mamRestTemplate;

    public void tryReportDownload(
            String operatorId,
            String resourceId,
            String resourceTitle,
            String destinationType,
            String actualDestinationType) {
        if (!StringUtils.hasText(mamConfigProperties.getDataEventBaseUrl())) {
            log.debug("dataEventBaseUrl empty, skip forward download event resourceId={}", resourceId);
            return;
        }
        JSONObject detail = new JSONObject();
        detail.put("resourceId", resourceId);
        detail.put("resourceTitle", resourceTitle);
        detail.put("destinationType", destinationType);
        if (StringUtils.hasText(actualDestinationType)) {
            detail.put("actualDestinationType", actualDestinationType);
        }
        String base = mamConfigProperties.getDataEventBaseUrl().trim().replaceAll("/+$", "");
        String url = base + "/api/v1/events";
        try {
            JSONObject body = new JSONObject();
            body.put("eventType", EVENT_MAM_DOWNLOAD);
            body.put("operator", operatorId);
            body.put("value", 1);
            body.put("detailJson", detail.toJSONString());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            mamRestTemplate.postForEntity(url, new HttpEntity<>(body.toJSONString(), headers), String.class);
        } catch (Exception e) {
            log.warn("forward download event to {} failed", url, e);
        }
    }
}
