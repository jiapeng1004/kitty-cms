package icu.jiapeng.kitty.plugin.s3.model.request;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@XmlRootElement(name = "Delete")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeleteObjectsRequest {
    @XmlElement(name = "Object")
    private List<ObjectItem> objects;

    @Data
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ObjectItem {
        @XmlElement(name = "Key")
        private String key;
    }
}
