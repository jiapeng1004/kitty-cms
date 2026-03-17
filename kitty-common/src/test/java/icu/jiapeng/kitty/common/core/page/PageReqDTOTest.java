package icu.jiapeng.kitty.common.core.page;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PageReqDTOTest {

    public static class TestPageReqDTO extends PageReqDTO<TestVo> {
    }

    public static class TestParentVo {
        private String pId;
    }

    public static class TestVo extends TestParentVo {
        private String cId;
    }

//    @Test
//    public void testEmpty() {
//        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
//        assertFalse(testPageReqDTO.illegalOrder(), "空的不违规");
//    }
//
//    @Test
//    public void testCid() {
//        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem("cId", PageReqDTO.Order.ASC)));
//        assertFalse(testPageReqDTO.illegalOrder(), "cId不应该违规");
//    }
//
//    @Test
//    public void testPid() {
//        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem("pId", PageReqDTO.Order.ASC)));
//        assertFalse(testPageReqDTO.illegalOrder(), "pId应该不违规");
//    }
//
//    @Test
//    public void testIllegal() {
//        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem("name", PageReqDTO.Order.ASC)));
//        assertTrue(testPageReqDTO.illegalOrder(), "非法字段应该违规");
//    }
//
//    @Test
//    public void validMethod() {
//        // 不违规的
//        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem("pId", PageReqDTO.Order.ASC)));
//        try {
//            testPageReqDTO.validOrder();
//        } catch (Exception e) {
//            fail("不违规的应该没有异常");
//        }
//        // 违规的
//        testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem("name", PageReqDTO.Order.ASC)));
//        try {
//            testPageReqDTO.validOrder();
//            fail("违规的应该有异常");
//        } catch (Exception e) {
//            // 忽略
//        }
//        // 一个违规一个不违规
//        testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem("pId", PageReqDTO.Order.ASC),
//                new PageReqDTO.OrderItem("name", PageReqDTO.Order.ASC)));
//        try {
//            testPageReqDTO.validOrder();
//            fail("违规的应该有异常");
//        } catch (Exception e) {
//            // 忽略
//        }
//    }
//
//    @Test
//    public void testUnion() {
//        // 测试逗号隔开字段的情况
//        // 1.两个合法字段
//        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem(" pId , cId", PageReqDTO.Order.ASC)));
//        assertFalse(testPageReqDTO.illegalOrder(), "两个合法字段应该不违规");
//        // 2.1个合法一个不合法
//        testPageReqDTO = new TestPageReqDTO();
//        testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem(" pId , name ", PageReqDTO.Order.ASC)));
//        assertTrue(testPageReqDTO.illegalOrder(), "一个合法一个不合法应该违规");
//    }


    @Test
    void hook() {
        // 测试逗号隔开字段的情况
        // 1.两个合法字段
        TestPageReqDTO testPageReqDTO = new TestPageReqDTO();
        try {
            testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem(" pId , cId", CommonOrder.ASC)));
        } catch (Exception e) {
            fail("两个合法字段应该不违规");
        }
        // 2.1个合法一个不合法
        testPageReqDTO = new TestPageReqDTO();
        try {
            testPageReqDTO.setOrders(Lists.newArrayList(new PageReqDTO.OrderItem(" pId , name ", CommonOrder.ASC)));
            fail("一个合法一个不合法应该违规");
        } catch (Exception e) {
            // 忽略
        }
    }
}