package icu.jiapeng.kitty.plugin.s3.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "DeleteResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeleteObjectsResponse {
    @XmlElement(name = "Deleted")
    private List<Deleted> deleted;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Deleted {
        @XmlElement(name = "Key")
        private String key;
    }
}
