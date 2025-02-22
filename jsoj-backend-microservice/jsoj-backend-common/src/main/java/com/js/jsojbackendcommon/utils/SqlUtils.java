package com.js.jsojbackendcommon.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * SQL 工具
 * @author sakisaki
 * @date 2025/2/22 14:40
 */
public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     *
     * @param sortField 排序类型
     * @return boolean
     */
    public static boolean validSortField(String sortField) {
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
