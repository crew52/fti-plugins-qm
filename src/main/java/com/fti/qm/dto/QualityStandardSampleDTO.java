package com.fti.qm.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fti.qm.BigDecimalDeserializer;

import java.math.BigDecimal;

public class QualityStandardSampleDTO {

    private Long id;
    private Long number;

    // Liên kết đến lệnh QC (Foreign Key)
    private Long qualityInspectionCommandRe;

    // Liên kết đến tiêu chuẩn chất lượng (Foreign Key)
    private Long qualityStandardL;

    // Các trường dữ liệu mẫu (dựa trên qualityStandardSampleRe.xml)
    private String position;
    private String qcNumber;
    private String qcName;
    private String qcType;
    private String unit;
    private String description;
    private Integer sampleSize;
    private Integer sampleNumber;

    private String qualitativeValue; // Enum: 01pass
    private String qualitativeResult; // Enum: 01pass, 02fail
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal quantitativeValue; // Decimal
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal upValue; // Decimal
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal downValue; // Decimal
    @JsonDeserialize(using = BigDecimalDeserializer.class)
    private BigDecimal quantitativeResult; // Decimal
    private String quantitativeEvaluation; // Enum: 01pass, 02fail
    private String meName;
    private String meMeasuringMethod;

    public QualityStandardSampleDTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public Long getQualityInspectionCommandRe() {
        return qualityInspectionCommandRe;
    }

    public void setQualityInspectionCommandRe(Long qualityInspectionCommandRe) {
        this.qualityInspectionCommandRe = qualityInspectionCommandRe;
    }

    public Long getQualityStandardL() {
        return qualityStandardL;
    }

    public void setQualityStandardL(Long qualityStandardL) {
        this.qualityStandardL = qualityStandardL;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getQcNumber() {
        return qcNumber;
    }

    public void setQcNumber(String qcNumber) {
        this.qcNumber = qcNumber;
    }

    public String getQcName() {
        return qcName;
    }

    public void setQcName(String qcName) {
        this.qcName = qcName;
    }

    public String getQcType() {
        return qcType;
    }

    public void setQcType(String qcType) {
        this.qcType = qcType;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(Integer sampleSize) {
        this.sampleSize = sampleSize;
    }

    public Integer getSampleNumber() {
        return sampleNumber;
    }

    public void setSampleNumber(Integer sampleNumber) {
        this.sampleNumber = sampleNumber;
    }

    public String getQualitativeValue() {
        return qualitativeValue;
    }

    public void setQualitativeValue(String qualitativeValue) {
        this.qualitativeValue = qualitativeValue;
    }

    public String getQualitativeResult() {
        return qualitativeResult;
    }

    public void setQualitativeResult(String qualitativeResult) {
        this.qualitativeResult = qualitativeResult;
    }

    public BigDecimal getQuantitativeValue() {
        return quantitativeValue;
    }

    public void setQuantitativeValue(BigDecimal quantitativeValue) {
        this.quantitativeValue = quantitativeValue;
    }

    public BigDecimal getUpValue() {
        return upValue;
    }

    public void setUpValue(BigDecimal upValue) {
        this.upValue = upValue;
    }

    public BigDecimal getDownValue() {
        return downValue;
    }

    public void setDownValue(BigDecimal downValue) {
        this.downValue = downValue;
    }

    public BigDecimal getQuantitativeResult() {
        return quantitativeResult;
    }

    public void setQuantitativeResult(BigDecimal quantitativeResult) {
        this.quantitativeResult = quantitativeResult;
    }

    public String getQuantitativeEvaluation() {
        return quantitativeEvaluation;
    }

    public void setQuantitativeEvaluation(String quantitativeEvaluation) {
        this.quantitativeEvaluation = quantitativeEvaluation;
    }

    public String getMeName() {
        return meName;
    }

    public void setMeName(String meName) {
        this.meName = meName;
    }

    public String getMeMeasuringMethod() {
        return meMeasuringMethod;
    }

    public void setMeMeasuringMethod(String meMeasuringMethod) {
        this.meMeasuringMethod = meMeasuringMethod;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + java.util.Objects.hashCode(this.id);
        hash = 67 * hash + java.util.Objects.hashCode(this.number);
        hash = 67 * hash + java.util.Objects.hashCode(this.qualityInspectionCommandRe);
        hash = 67 * hash + java.util.Objects.hashCode(this.qualityStandardL);
        hash = 67 * hash + java.util.Objects.hashCode(this.position);
        hash = 67 * hash + java.util.Objects.hashCode(this.qcNumber);
        hash = 67 * hash + java.util.Objects.hashCode(this.qcName);
        hash = 67 * hash + java.util.Objects.hashCode(this.qcType);
        hash = 67 * hash + java.util.Objects.hashCode(this.unit);
        hash = 67 * hash + java.util.Objects.hashCode(this.description);
        hash = 67 * hash + java.util.Objects.hashCode(this.sampleSize);
        hash = 67 * hash + java.util.Objects.hashCode(this.sampleNumber);
        hash = 67 * hash + java.util.Objects.hashCode(this.qualitativeValue);
        hash = 67 * hash + java.util.Objects.hashCode(this.quantitativeValue);
        hash = 67 * hash + java.util.Objects.hashCode(this.upValue);
        hash = 67 * hash + java.util.Objects.hashCode(this.downValue);
        hash = 67 * hash + java.util.Objects.hashCode(this.qualitativeResult);
        hash = 67 * hash + java.util.Objects.hashCode(this.quantitativeResult);
        hash = 67 * hash + java.util.Objects.hashCode(this.quantitativeEvaluation);
        hash = 67 * hash + java.util.Objects.hashCode(this.meName);
        hash = 67 * hash + java.util.Objects.hashCode(this.meMeasuringMethod);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final QualityStandardSampleDTO other = (QualityStandardSampleDTO) obj;
        if (!java.util.Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!java.util.Objects.equals(this.number, other.number)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qualityInspectionCommandRe, other.qualityInspectionCommandRe)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qualityStandardL, other.qualityStandardL)) {
            return false;
        }
        if (!java.util.Objects.equals(this.position, other.position)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qcNumber, other.qcNumber)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qcName, other.qcName)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qcType, other.qcType)) {
            return false;
        }
        if (!java.util.Objects.equals(this.unit, other.unit)) {
            return false;
        }
        if (!java.util.Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!java.util.Objects.equals(this.sampleSize, other.sampleSize)) {
            return false;
        }
        if (!java.util.Objects.equals(this.sampleNumber, other.sampleNumber)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qualitativeValue, other.qualitativeValue)) {
            return false;
        }
        if (!java.util.Objects.equals(this.quantitativeValue, other.quantitativeValue)) {
            return false;
        }
        if (!java.util.Objects.equals(this.upValue, other.upValue)) {
            return false;
        }
        if (!java.util.Objects.equals(this.downValue, other.downValue)) {
            return false;
        }
        if (!java.util.Objects.equals(this.qualitativeResult, other.qualitativeResult)) {
            return false;
        }
        if (!java.util.Objects.equals(this.quantitativeResult, other.quantitativeResult)) {
            return false;
        }
        if (!java.util.Objects.equals(this.quantitativeEvaluation, other.quantitativeEvaluation)) {
            return false;
        }
        if (!java.util.Objects.equals(this.meName, other.meName)) {
            return false;
        }
        if (!java.util.Objects.equals(this.meMeasuringMethod, other.meMeasuringMethod)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "QualityStandardSampleDTO{" +
                "id=" + id +
                ", qualityInspectionCommandRe=" + qualityInspectionCommandRe +
                ", qualityStandardL=" + qualityStandardL +
                ", position='" + position + '\'' +
                ", qcNumber='" + qcNumber + '\'' +
                ", qcName='" + qcName + '\'' +
                ", qcType='" + qcType + '\'' +
                ", unit='" + unit + '\'' +
                ", description='" + description + '\'' +
                ", sampleSize=" + sampleSize +
                ", sampleNumber=" + sampleNumber +
                ", qualitativeResult='" + qualitativeResult + '\'' +
                ", quantitativeValue=" + quantitativeValue +
                ", upValue=" + upValue +
                ", downValue=" + downValue +
                ", quantitativeResult=" + quantitativeResult +
                ", quantitativeEvaluation='" + quantitativeEvaluation + '\'' +
                ", meName='" + meName + '\'' +
                ", meMeasuringMethod='" + meMeasuringMethod + '\'' +
                '}';
    }
}
