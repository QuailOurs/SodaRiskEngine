package com.soda.risk.engine.core.strategy.engine.function;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 表达式操作类型枚举
 * 定义规则表达式中支持的所有操作符
 *
 * @since 2019.05.30
 * @version 2.0
 */
@Getter
public enum ExpressionOperatorTypeEnum {

    EQUAL(1, "==", "等于", false),
    NOT_EQUAL(2, "!=", "不等于", false),
    EQUAL_STRING(3, "EQUAL_STRING", "等于字符串", false),
    NOT_EQUAL_STRING(4, "NOT_EQUAL_STRING", "不等于字符串", false),
    EQUAL_NULL_OBJECT(5, "EQUAL_NULL_OBJECT", "等于空对象", false),
    MORE_THAN(6, ">", "大于", false),
    LESS_THAN(7, "<", "小于", false),
    MORE_THAN_AND_EQUAL(8, ">=", "大于等于", false),
    LESS_THAN_AND_EQUAL(9, "<=", "小于等于", false),
    INCLUDE(10, "INCLUDE", "包含", true),
    NOT_INCLUDE(11, "NOT_INCLUDE", "不包含", true),
    STARTS_WITH(12, "STARTS_WITH", "以...开头", true),
    END_WITH(13, "END_WITH", "以...结尾", true),
    INTERSECTION(14, "INTERSECTION", "字符串数组交集", false),
    REG(15, "REGEXP", "正则表达式", false),
    FIELD_EQUAL(16, "FIELD_EQUAL", "字段相等", false),
    FIELD_NOT_EQUAL(17, "FIELD_NOT_EQUAL", "字段不相等", false),
    FIELD_EXISTS(18, "FIELD_EXISTS", "字段存在", false),
    FIELD_NOT_EXISTS(19, "FIELD_NOT_EXISTS", "字段不存在", false),
    LENGTH_MORE_THAN_AND(20, "LENGTH_MORE_THAN_AND", "字符串长度大于等于", false),
    LENGTH_LESS_THAN_AND(21, "LENGTH_LESS_THAN_AND", "字符串长度小于等于", false),
    INDEX_MERGE_MORE_THAN(22, "INDEX_MERGE_MORE_THAN", "指标合并大于", false),
    INDEX_MERGE_LESS_THAN(23, "INDEX_MERGE_LESS_THAN", "指标合并小于", false),
    INDEX_MERGE_EQUAL(24, "INDEX_MERGE_EQUAL", "指标合并等于", false),
    INDEX_MERGE_MORE_EQUAL(25, "INDEX_MERGE_MORE_EQUAL", "指标合并大于等于", false),
    INDEX_MERGE_LESS_EQUAL(26, "INDEX_MERGE_LESS_EQUAL", "指标合并小于等于", false),
    FIELD_INCLUDE(27, "FIELD_INCLUDE", "字段包含", true),
    FIELD_NOT_INCLUDE(28, "FIELD_NOT_INCLUDE", "字段不包含", true);

    private final int code;
    private String value;
    private final String desc;
    private final boolean needSplit;

    public static final String SEPARATOR = ",";

    ExpressionOperatorTypeEnum(int code, String value, String desc, boolean needSplit) {
        this.code = code;
        this.value = value;
        this.desc = desc;
        this.needSplit = needSplit;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ExpressionOperatorTypeEnum getEnumByValue(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        for (ExpressionOperatorTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return EQUAL;
    }

    /**
     * 根据操作符字符串获取操作符枚举
     */
    public static ExpressionOperatorTypeEnum getTypeEnum(String expressOperator) {
        return Objects.requireNonNull(getEnumByValue(expressOperator));
    }

    /**
     * 生成表达式字符串
     *
     * @param expressOperator 表达式操作符
     * @param sequence        表达式左操作数
     * @param searchString    表达式右操作数
     * @return 表达式字符串
     */
    public static String createExpression(String expressOperator, CharSequence sequence, CharSequence searchString) {
        if (StringUtils.isBlank(expressOperator)) {
            return null;
        }
        ExpressionOperatorTypeEnum type = getTypeEnum(expressOperator);
        CharSequence[] searchStrings = new String[]{searchString.toString()};
        if (type.isNeedSplit()) {
            searchStrings = StringUtils.split(searchString.toString(), SEPARATOR);
        }
        return type.buildExpression(sequence, searchStrings);
    }

    /**
     * 根据操作符类型生成对应的Aviator表达式字符串
     *
     * @param fieldKey     字段名
     * @param operator     操作符
     * @param value        值
     * @return 表达式字符串，如 "fieldName > 100" 或 "INCLUDE(fieldName,'a,b,c')"
     */
    public static String buildRuleExpression(String fieldKey, String operator, String value) {
        ExpressionOperatorTypeEnum type = getEnumByValue(operator);
        if (type == null) {
            return fieldKey + " " + operator + " " + value;
        }
        if (type.isNeedSplit()) {
            return type.getValue() + "(" + fieldKey + ",'" + value + "')";
        }
        // 正则
        if (type == REG) {
            return fieldKey + "=~/" + value + "/";
        }
        // 字段操作符
        if (type == FIELD_EQUAL || type == FIELD_NOT_EQUAL || type == FIELD_EXISTS
                || type == FIELD_NOT_EXISTS || type == FIELD_INCLUDE || type == FIELD_NOT_INCLUDE) {
            return type.getValue() + "('" + fieldKey + "','" + value + "')";
        }
        // 空对象判断
        if (type == EQUAL_NULL_OBJECT) {
            return type.getValue() + "(" + fieldKey + ",'" + value + "')";
        }
        // 字符串操作
        if (type == EQUAL_STRING || type == NOT_EQUAL_STRING) {
            return type.getValue() + "(" + fieldKey + ",'" + value + "')";
        }
        // 长度操作
        if (type == LENGTH_MORE_THAN_AND || type == LENGTH_LESS_THAN_AND) {
            return type.getValue() + "('" + fieldKey + "'," + value + ")";
        }
        // 指标合并操作
        if (type.name().startsWith("INDEX_MERGE")) {
            return type.getValue() + "('" + fieldKey + "','" + value + "')";
        }
        // 基本操作符 ==, !=, >, <, >=, <=
        return fieldKey + " " + type.getValue() + " " + value;
    }

    private String buildExpression(CharSequence sequence, CharSequence... searchStrings) {
        if (needSplit || searchStrings.length != 1) {
            String searchStringsStr = StringUtils.join(searchStrings, SEPARATOR);
            return this.getValue() + "(" + sequence + ",'" + searchStringsStr + "')";
        }
        return sequence + this.getValue() + searchStrings[0];
    }
}
