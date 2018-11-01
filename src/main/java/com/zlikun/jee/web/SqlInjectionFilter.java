package com.zlikun.jee.web;

import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * SQL 注入过滤器 <br>
 *
 * <h3>SQL注入的几种方式：</h3>
 * <pre>
 * 1) 使用 ' or 语句，将查询条件扩大，实现破坏性查询（操作）
 * 2) 使用 ; 将SQL分成两部分，在后面一部分实现破坏性操作
 * 3) 使用注释，将后面的条件取消掉，将查询条件扩大，注意MySQL有三种注释的方法，都需要处理
 *
 * 为了简化处理，这里只考虑字符串类型参数注入情况（整型等其它类型在应用内部类型转换会失败，所以基本可以忽略）
 * </pre>
 *
 * @author zlikun
 * @date 2018/11/1 9:50
 */
@Order(1)
@WebFilter(urlPatterns = "/*")
public class SqlInjectionFilter implements Filter {

    // SQL语法检查正则：只检查一个关键字可能存在误判情况，这里要求必须符合两个关键字（有先后顺序）才算匹配
    // 第一组关键字
    final String group1 = "insert|delete|update|select|create|drop|truncate|grant|alter|deny|revoke|call|execute|exec|declare|show|rename|set";
    // 第二组关键字
    final String group2 = "into|from|set|where|table|database|view|index|on|cursor|procedure|trigger|for|password|union|and|or";
    // 构造SQL语法检查正则
    final Pattern sqlSyntaxPattern = Pattern.compile("(" + group1 + ").+(" + group2 + ")", Pattern.CASE_INSENSITIVE);
    // 使用'、;或注释截断SQL检查正则
    final Pattern sqlCommentPattern = Pattern.compile("'.*(or|union|--|#|/*|;)", Pattern.CASE_INSENSITIVE);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 检查查询参数
        Map<String, String[]> params = req.getParameterMap();
        if (!CollectionUtils.isEmpty(params)) {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (entry.getValue() == null) continue;
                for (String value : entry.getValue()) {
                    if (!validate(value)) {
                        errorResponse(resp);
                        return;
                    }
                }
            }
        }

        // 检查请求Body中的参数（使用 RequestBodyWrapper 规避流被读取后，后续无法再次读取问题）
        RequestBodyWrapper rw = new RequestBodyWrapper(req);
        // 整个JSON作为一个值来检查（出于性能考虑，但会增加误判风险）
        if (!validate(rw.getBody())) {
            errorResponse(resp);
            return;
        }

        chain.doFilter(rw, resp);

    }

    private void errorResponse(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html");
        response.sendError(SC_BAD_REQUEST, "请求参数包含敏感字符，服务器拒绝请求");
    }

    /**
     * 检查参数是否合法
     *
     * @param value
     * @return
     */
    private boolean validate(String value) {
        // 空值一定不包含敏感字符，所以认为合法
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        // 处理是否包含SQL注释字符
        if (sqlCommentPattern.matcher(value).find()) {
            return false;
        }
        // 检查是否包含SQL注入敏感字符
        if (sqlSyntaxPattern.matcher(value).find()) {
            return false;
        }

        return true;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

}
