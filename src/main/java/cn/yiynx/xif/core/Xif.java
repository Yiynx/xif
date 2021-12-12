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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;


/**
 * Xif（if扩展，if处理解耦）
 * @author www@yiynx.cn
 */
public class Xif {
    private static final Logger log = LoggerFactory.getLogger(Xif.class);
    private static final Map<String, List<XifHandler>> XIF_HANDLER_MAP = new HashMap<>();
    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final Map<String, Boolean> CHECK_XIF_HANDLER_REPEAT = new HashMap<>();
    /**
     * 注册Xif处理
     * @param xifHandler xif处理棋
     */
    public static void register(XifHandler xifHandler) {
        log.info("Xif-register->group:{}, condition:{}", xifHandler.getGroup(), xifHandler.getCondition());
        Assert.hasText(xifHandler.getGroup(), "xif-group not empty");

        preParseExpression(xifHandler.getCondition());

        String UNIQUE_KEY = xifHandler.getGroup().concat("#").concat(xifHandler.getCondition());
        log.debug("XIF-UNIQUE_KEY:{}", UNIQUE_KEY);
        Assert.isTrue(CHECK_XIF_HANDLER_REPEAT.putIfAbsent(UNIQUE_KEY, Boolean.TRUE) == null, "xif-group:condition already exists");

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
        if (!StringUtils.hasText(condition)) {
            return;
        }
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
     * @return 处理结果
     */
    public static <T> Object handler(String group, T param) {
        log.debug("group:{}, param:{}", group, param);
        ExpressionParser parser = new SpelExpressionParser();
        Optional<XifHandler> xifHandlerOptional = Optional.ofNullable(XIF_HANDLER_MAP.get(group))
                .orElseThrow(() -> new IllegalArgumentException("xif-group（"+group+"）not found"))
                .stream()
                .filter(xifHandler -> {
                    try {
                        log.debug("group:{}, condition:{}, param:{}", xifHandler.getGroup(), xifHandler.getCondition(), param);
                        if (xifHandler.isElse()) { // xif-else
                            return false;
                        }
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
                }).findAny();
        // if
        if (xifHandlerOptional.isPresent()) {
            return xifHandlerOptional.get().handler(param);
        }
        // else
        xifHandlerOptional = XIF_HANDLER_MAP.get(group).stream().filter(XifHandler::isElse).findAny();
        if (xifHandlerOptional.isPresent()) {
            return xifHandlerOptional.get().handler(param);
        }
        log.debug("xif-handler-other: return null");
        return null;
    }
}
