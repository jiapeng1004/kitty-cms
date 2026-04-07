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
@XmlRootElement(name = "ListAllMyBucketsResult")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListBucketsResponse {
    @XmlElement(name = "Buckets")
    private Buckets buckets;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Buckets {
        @XmlElement(name = "Bucket")
        private List<Bucket> bucketList;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Bucket {
        @XmlElement(name = "Name")
        private String name;

        @XmlElement(name = "CreationDate")
        @XmlSchemaType(name = "dateTime")
        private Date creationDate;
    }
}
