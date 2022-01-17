package com.fmrt.guid;

import com.fmrt.common.guid.App;
import com.fmrt.common.guid.service.GuidGenService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/10/13 15:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = App.class)
public class GuidTest {
    @Autowired
    private GuidGenService guidGenService;

    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Test
    public void guidGen() {
        System.out.println(1 << 3);
        while (true) {
            executorService.execute(() -> {
                try {
                    var gm = guidGenService.gen();
                    var guid = gm.block();
                    System.out.println(guid);
                    System.out.println(guidGenService.expand(guid).block());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
