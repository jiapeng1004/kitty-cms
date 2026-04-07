package icu.jiapeng.kitty.plugin.s3.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "InitiateMultipartUploadResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class InitiateMultipartUploadResponse {
    @XmlElement(name = "Bucket")
    private String bucket;
    
    @XmlElement(name = "Key")
    private String key;
    
    @XmlElement(name = "UploadId")
    private String uploadId;
}
