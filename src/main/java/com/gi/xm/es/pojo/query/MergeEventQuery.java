package com.gi.xm.es.pojo.query;

import com.gi.xm.es.pojo.Pagination;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zcy on 17-4-18.
 */
public class MergeEventQuery extends Pagination implements Serializable {

    private Long id;

    private String code;

    private Long sourceId;

    private Long sourceCode;

    private String  industryName;

    private String  industrySubName;

    private String districtSubName;

    private String mergeType;

    private String mergeState;

    private String currencyType;

    private Integer equityRate;

    private String mergeDate;

    private String logo;

    private String projTitle;

    private String amountStr;

    private Integer amountNum;

    private String mergeSideJson;

    private String bodyRole;

    private String sourceType;

    private String isClick;

    private String startDate;

    private String endDate;

    private String order;

    private String orderBy;

    private List<String> currencyTypes;

    private List<String> industryIds;

    private List<String> equityRates;

    private List<String> mergeTypes;

    private List<String> mergeStates;

    public String getMergeType() {
        return mergeType;
    }

    public void setMergeType(String mergeType) {
        this.mergeType = mergeType;
    }

    public String getDistrictSubName() {
        return districtSubName;
    }

    public void setDistrictSubName(String districtSubName) {
        this.districtSubName = districtSubName;
    }

    public List<String> getIndustryIds() {
        return industryIds;
    }

    public void setIndustryIds(List<String> industryIds) {
        this.industryIds = industryIds;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getMergeState() {
        return mergeState;
    }

    public void setMergeState(String mergeState) {
        this.mergeState = mergeState;
    }

    public Integer getEquityRate() {
        return equityRate;
    }

    public void setEquityRate(Integer equityRate) {
        this.equityRate = equityRate;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getProjTitle() {
        return projTitle;
    }

    public void setProjTitle(String projTitle) {
        this.projTitle = projTitle;
    }

    public String getAmountStr() {
        return amountStr;
    }

    public void setAmountStr(String amountStr) {
        this.amountStr = amountStr;
    }

    public Integer getAmountNum() {
        return amountNum;
    }

    public void setAmountNum(Integer amountNum) {
        this.amountNum = amountNum;
    }

    public String getMergeDate() {
        return mergeDate;
    }

    public void setMergeDate(String mergeDate) {
        this.mergeDate = mergeDate;
    }

    public String getMergeSideJson() {
        return mergeSideJson;
    }

    public void setMergeSideJson(String mergeSideJson) {
        this.mergeSideJson = mergeSideJson;
    }

    public String getBodyRole() {
        return bodyRole;
    }

    public void setBodyRole(String bodyRole) {
        this.bodyRole = bodyRole;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getIsClick() {
        return isClick;
    }

    public void setIsClick(String isClick) {
        this.isClick = isClick;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(Long sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public String getIndustrySubName() {
        return industrySubName;
    }

    public void setIndustrySubName(String industrySubName) {
        this.industrySubName = industrySubName;
    }

    public List<String> getMergeTypes() {
        return mergeTypes;
    }

    public void setMergeTypes(List<String> mergeTypes) {
        this.mergeTypes = mergeTypes;
    }

    public List<String> getMergeStates() {
        return mergeStates;
    }

    public void setMergeStates(List<String> mergeStates) {
        this.mergeStates = mergeStates;
    }

    public List<String> getEquityRates() {
        return equityRates;
    }

    public void setEquityRates(List<String> equityRates) {
        this.equityRates = equityRates;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public List<String> getCurrencyTypes() {
        return currencyTypes;
    }

    public void setCurrencyTypes(List<String> currencyTypes) {
        this.currencyTypes = currencyTypes;
    }
}
