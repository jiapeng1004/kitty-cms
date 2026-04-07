package icu.jiapeng.kitty.plugin.s3.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "Error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorResponse {
    @XmlElement(name = "Code")
    private String code;
    
    @XmlElement(name = "Message")
    private String message;
}
