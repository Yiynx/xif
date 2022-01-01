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

    private Xif() {}

    /**
     * 注册Xif处理
     * @param xifHandler xif处理棋
     */
    public static void register(XifHandler xifHandler) {
        log.info("Xif-register->group:{}, condition:{}", xifHandler.getGroup(), xifHandler.getCondition());
        Assert.hasText(xifHandler.getGroup(), "xif-group not empty");

        preParseExpression(xifHandler.getCondition());

        String uniqueKey = xifHandler.getGroup().concat("->").concat(StringUtils.hasText(xifHandler.getCondition()) ? xifHandler.getCondition() : "else");
        log.debug("XIF-uniqueKey:{}", uniqueKey);
        Assert.isTrue(CHECK_XIF_HANDLER_REPEAT.putIfAbsent(uniqueKey, Boolean.TRUE) == null, "xif-group:condition already exists");

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
            log.error("XifHandler-condition-error！");
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
                .orElseThrow(() -> new IllegalArgumentException("xif-group（" + group + "）not found"))
                .stream()
                .filter(XifHandler::isIf)
                .filter(xifHandler -> {
                    Expression expression = parser.parseExpression(xifHandler.getCondition());
                    EvaluationContext context = new StandardEvaluationContext();
                    context.setVariable(xifHandler.getParamName(), param);
                    Boolean isConditionPass;
                    try {
                        isConditionPass = expression.getValue(context, Boolean.class);
                    } catch (EvaluationException e) {
                        log.error("xif-condition error（The result is not of type Boolean）！group:{}, condition:{}, param:{}", xifHandler.getGroup(), xifHandler.getCondition(), param);
                        throw e;
                    }
                    if (Boolean.FALSE.equals(isConditionPass)) {
                        log.debug("group:{}, condition:{}, param:{}, is-xif-condition-pass:{}", xifHandler.getGroup(), xifHandler.getCondition(), param, isConditionPass);
                    }
                    return isConditionPass;
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
        log.debug("group:{}, param:{}（xif-handler-no-match: return null）", group, param);
        return null;
    }
}
