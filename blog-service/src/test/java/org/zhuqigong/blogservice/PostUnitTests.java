package org.zhuqigong.blogservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.Tag;

import java.util.Arrays;
import java.util.Date;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.MethodName.class)
class PostUnitTests {
    private static final Logger LOG = LoggerFactory.getLogger(PostUnitTests.class);
    @Autowired
    private MockMvc mvc;

    @Test
    @Order(1)
    void createNewPostTest() throws Exception {
        Post post = new Post();
        post.setAuthor("k");
        post.setContentBody("This is body");
        post.setTitle("title");
        post.setPublishTime(new Date());
        post.setCategories(Arrays.asList(new Category("c1"), new Category("c2")));
        post.setTags(Arrays.asList(new Tag("t1"), new Tag("t2")));
        Post post1 = new Post();
        BeanUtils.copyProperties(post, post1);
        post1.setTitle("title1");
        post1.setCategories(Arrays.asList(new Category("c1"), new Category("c3")));
        post1.setTags(Arrays.asList(new Tag("t2"), new Tag("t5")));
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(post);
        String content1 = mapper.writeValueAsString(post1);
        for (String c : Arrays.asList(content, content1)) {
            mvc.perform(MockMvcRequestBuilders.post("/blog/post/create").content(c)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.author").value("k"));
        }
    }

    @Test
    @Order(2)
    void getPostByPostIdTest() throws Exception {
        long postId = 1;
        mvc.perform(MockMvcRequestBuilders.get("/blog/post/id/" + postId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));
    }

    @Test
    @Order(3)
    void getPostByPostTitle() throws Exception {
        String postTitle = "title";
        mvc.perform(MockMvcRequestBuilders.get("/blog/post/title/" + postTitle))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("title"));
    }

    @Test
    @Order(4)
    void getPostsTest() throws Exception {
        String page = "1";
        String size = "10";
        String result = mvc.perform(
                MockMvcRequestBuilders.get("/blog/posts").param("page", page).param("size", size))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPage").value(1))
                .andReturn().getResponse()
                .getContentAsString();
        System.out.printf("Get posts return results : %s%n", result);
    }

    @Test
    @Order(5)
    void getCategoriesTest() throws Exception {
        String result = mvc.perform(MockMvcRequestBuilders.get("/blog/categories"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andReturn()
                .getResponse().getContentAsString();
        System.out.printf("Get categories result:%s%n", result);
    }

    @Test
    @Order(6)
    void getTagsTest() throws Exception {
        String result = mvc.perform(MockMvcRequestBuilders.get("/blog/tags"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andReturn()
                .getResponse().getContentAsString();
        System.out.printf("Get categories result:%s%n", result);
    }

    @Test
    @Order(7)
    void getPostByCategoryNameTest() throws Exception {
        String categoryName = "c1";
        String result =
                mvc.perform(MockMvcRequestBuilders.get("/blog/category/" + categoryName).param("page", "1")
                        .param("size", "3"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(MockMvcResultMatchers.jsonPath("$.totalPage").value(1))
                        .andReturn()
                        .getResponse().getContentAsString();
        LOG.info("Query Category:[{}] Post Result:{}", categoryName, result);
    }

    @Test
    @Order(9)
    void getPostByTagNameTest() throws Exception {
        String tagName = "t1";
        String result = mvc.perform(
                MockMvcRequestBuilders.get("/blog/tag/" + tagName).param("page", "1").param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPage").value(1))
                .andReturn().getResponse().getContentAsString();
        LOG.info("Query Tag:[{}] Post Result:{}", tagName, result);
    }
}
