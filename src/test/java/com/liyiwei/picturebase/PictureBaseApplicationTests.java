package com.liyiwei.picturebase;

import com.liyiwei.picturebase.model.entity.UserRoleEnum;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PictureBaseApplicationTests {

    @Test
    void contextLoads() {
        UserRoleEnum.getEnumByValue("user");
    }

}
