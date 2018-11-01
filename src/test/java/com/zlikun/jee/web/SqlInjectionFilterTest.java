package com.zlikun.jee.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author zlikun
 * @date 2018/11/1 16:37
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class SqlInjectionFilterTest {

    @Autowired
    MockMvc mvc;

    @Test
    public void doFilter200() throws Exception {
        // 不进行SQL注入
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post("/action", "v=1.0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\", \"password\":\"123456\"}")
        ).andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertEquals(SC_OK, response.getStatus());

        // 经过过滤器后，Body仍能被正确获取（验证ServletInputStream只能读取一遍是否解决）
        assertEquals("{\"username\":\"admin\",\"password\":\"123456\"}", response.getContentAsString());
    }

    @Test
    public void doFilter400() throws Exception {
        // 进行SQL注入
        MvcResult result = mvc.perform(MockMvcRequestBuilders
                .post("/action", "v=1.0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\", \"password\":\"' OR 1 = 1 -- \"}")
        ).andReturn();

        MockHttpServletResponse response = result.getResponse();
        assertEquals(SC_BAD_REQUEST, response.getStatus());
    }

    @Slf4j
    @RestController
    @SpringBootApplication
    @EnableSqlInjectionFilter
    static class Application {

        @RequestMapping(value = "/action", method = {GET, POST})
        public Map<String, Object> action(@RequestBody Map<String, Object> data) {
            System.out.println("-------<");
            System.out.println(data);
            System.out.println(">-------");
            return data;
        }

        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }

    }

}