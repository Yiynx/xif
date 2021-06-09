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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;


/**
 * Xif（if扩展，if处理解耦）
 * @author www@yiynx.cn
 */
public class Xif {
    private static final Logger log = LoggerFactory.getLogger(Xif.class);
    private static final Map<String, List<XifHandler>> XIF_HANDLER_MAP = new HashMap<>();
    private static final ExpressionParser parser = new SpelExpressionParser();
    /**
     * 注册Xif处理
     * @param xifHandler xif处理棋
     */
    public static void register(XifHandler xifHandler) {
        log.info("Xif-register->group:{}, condition:{}", xifHandler.getGroup(), xifHandler.getCondition());
        preParseExpression(xifHandler.getCondition());
        if (!XIF_HANDLER_MAP.containsKey(xifHandler.getGroup())) {
            XIF_HANDLER_MAP.put(xifHandler.getGroup(), new ArrayList<>());
        }
        XIF_HANDLER_MAP.get(xifHandler.getGroup()).add(xifHandler);
        log.debug("xif-handler:{}", XIF_HANDLER_MAP);
    }

    /**
     * 解析表达式
     * @param condition xif条件
     */
    private static void preParseExpression(String condition) {
        try {
            parser.parseExpression(condition);
        } catch (ParseException e) {
            log.error("XifHandler-condition-error！", e);
            throw e;
        }
    }

    /**
     * 处理
     * @param group xif分组名称
     * @param param xif参数
     * @param <T> xif参数泛型
     */
    public static <T> void handler(String group, T param) {
        log.debug("group:{}, param:{}", group, param);
        ExpressionParser parser = new SpelExpressionParser();
        Optional.ofNullable(XIF_HANDLER_MAP.get(group))
                .orElseThrow(() -> new IllegalArgumentException("xif-group（"+group+"）not found"))
                .stream()
                .filter(xifHandler -> {
                    try {
                        log.debug("group:{}, condition:{}, param:{}", xifHandler.getGroup(), xifHandler.getCondition(), param);
                        Expression expression = parser.parseExpression(xifHandler.getCondition());
                        EvaluationContext context = new StandardEvaluationContext();
                        context.setVariable(xifHandler.getParamName(), param);
                        boolean isConditionPass = expression.getValue(context, Boolean.class);
                        log.debug("group:{}, condition:{}, param:{}, is-xif-condition-pass:{}", xifHandler.getGroup(), xifHandler.getCondition(), param, isConditionPass);
                        return isConditionPass;
                    } catch (EvaluationException e) {
                        log.error("xifHandler-filter-error", e);
                    }
                    return false;
                }).forEach(xifHandler -> xifHandler.handler(param));
    }
}
