package com.zlikun.jee.web;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author zlikun
 * @date 2018/11/1 10:09
 */
public class RequestBodyWrapper extends HttpServletRequestWrapper {

    /**
     * JSON 字符串
     */
    private String body;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public RequestBodyWrapper(HttpServletRequest request) {
        super(request);

        // 获取JSON请求Body
        if (isJsonContentType()) {
            try {
                this.body = getJsonRequestBody(request.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 检查Content-Type是否为application/json
     *
     * @return
     */
    private boolean isJsonContentType() {
        String contentType = this.getHeader("Content-Type");
        return contentType != null && contentType.toLowerCase().startsWith("application/json");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (!isJsonContentType()) {
            return super.getInputStream();
        }
        return new ServletInputStream() {

            ByteArrayInputStream input = null;

            {
                if (body != null) {
                    input = new ByteArrayInputStream(body.getBytes());
                }
            }

            @Override
            public int read() throws IOException {
                if (input == null) {
                    return -1;
                } else {
                    return input.read();
                }
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }
        };
    }

    /**
     * 解析JSON请求Body，并重新封装ServletInputStream，使之可以被重复读取
     *
     * @param input
     * @return
     */
    private String getJsonRequestBody(ServletInputStream input) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            StringBuilder builder = new StringBuilder();
            reader.lines().forEach(builder::append);
            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getBody() {
        return this.body;
    }

}
