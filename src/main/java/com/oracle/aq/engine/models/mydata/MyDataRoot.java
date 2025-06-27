package com.oracle.aq.engine.models.mydata;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name ="root")
public class MyDataRoot {
    @XmlElement(name="resultStatus")
private ResultStatus resultStatus;
}
