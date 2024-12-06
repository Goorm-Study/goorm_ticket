package com.example.shared;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.shared.api.coupon.service.distribute.DistributedLockAop;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BeanLoadTest {

    @Autowired(required = false)
    private DistributedLockAop distributedLockAop;

    @Test
    void testAopBeanLoaded() {
        assertThat(distributedLockAop).isNotNull();
    }
}
