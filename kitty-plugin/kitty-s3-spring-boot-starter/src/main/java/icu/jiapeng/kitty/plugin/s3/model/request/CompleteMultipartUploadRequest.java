package icu.jiapeng.kitty.plugin.s3.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "CompleteMultipartUpload")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompleteMultipartUploadRequest {
    @XmlElement(name = "Part")
    private List<PartItem> parts;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PartItem {
        @XmlElement(name = "PartNumber")
        private Integer partNumber;

        @XmlElement(name = "ETag")
        private String eTag;
    }
}
