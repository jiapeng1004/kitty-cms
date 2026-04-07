package icu.jiapeng.kitty.plugin.s3.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "CompleteMultipartUploadResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class CompleteMultipartUploadResponse {
    @XmlElement(name = "Location")
    private String location;
    
    @XmlElement(name = "Bucket")
    private String bucket;
    
    @XmlElement(name = "Key")
    private String key;
    
    @XmlElement(name = "ETag")
    private String eTag;
}
