package com.oracle.aq.engine.models.mydata;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultStatus {
    @XmlElement(name="statusCode")
    private String statusCode;
    @XmlElement(name="errorCode")
    private String errorCode;
    @XmlElement(name="errorDescription")
    private String errorDescription;
    @XmlElement(name="errorCategory")
    private String errorCategory;
}
