/**
 * Copyright (c) 2021 yiynx.cn
 * Yiynx xif is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package cn.yiynx.xif.core;

import org.springframework.util.StringUtils;

/**
 * Xif处理器接口
 * @author www@yiynx.cn
 */
public interface XifHandler {

    /**
     * 获取分组
     * @return xif分组名称
     */
    String getGroup();

    /**
     * 获取处理器执行调节(SpEL表达式)
     * @return xif条件
     */
    String getCondition();

    default boolean isElse() {
        return !StringUtils.hasText(getCondition());
    }

    /**
     * Xif监听注解的方法
     * @return xif条件参数名称
     */
    String getParamName();

    /**
     * Xif处理方法
     * @param param 参数
     * @param <T> xif参数泛型
     */
    <T> Object handler(T param);
}
