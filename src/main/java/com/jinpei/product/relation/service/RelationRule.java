package com.jinpei.product.relation.service;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品之间的关联规则
 * Created by liuzhaoming on 2017/8/30.
 */
@Data
@NoArgsConstructor
public class RelationRule implements Serializable, Comparable<RelationRule> {

    /**
     * 前项
     */
    private String antecedent;

    /**
     * 后项
     */
    private String consequent;

    /**
     * 规则的置信度
     */
    private Double confidence;


    /**
     * 前项名称
     */
    private String antecedentName;

    /**
     * 后项名称
     */
    private String consequentName;

    /**
     * 前项用户数
     */
    private int antecedentCustomerNum;

    public RelationRule(String antecedent, String consequent, Double confidence) {
        this.antecedent = antecedent;
        this.consequent = consequent;
        this.confidence = confidence;
    }


    @Override
    public int compareTo(RelationRule o) {
        if (null == o) {
            return 1;
        }

        if (null == confidence) {
            return -1;
        }

        return confidence.compareTo(o.getConfidence());
    }
}
