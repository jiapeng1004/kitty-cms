package icu.jiapeng.kitty.plugin.s3.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "ListBucketResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListObjectsV2Response {
    @XmlElement(name = "Name")
    private String name;
    
    @XmlElement(name = "Prefix")
    private String prefix;
    
    @XmlElement(name = "ContinuationToken")
    private String continuationToken;
    
    @XmlElement(name = "NextContinuationToken")
    private String nextContinuationToken;
    
    @XmlElement(name = "MaxKeys")
    private int maxKeys;
    
    @XmlElement(name = "IsTruncated")
    private boolean isTruncated;
    
    @XmlElement(name = "Contents")
    private List<Contents> contents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Contents {
        @XmlElement(name = "Key")
        private String key;
        
        @XmlElement(name = "LastModified")
        @XmlSchemaType(name = "dateTime")
        private Date lastModified;
        
        @XmlElement(name = "ETag")
        private String eTag;
        
        @XmlElement(name = "Size")
        private long size;
        
        @XmlElement(name = "StorageClass")
        private String storageClass = "STANDARD";
    }
}