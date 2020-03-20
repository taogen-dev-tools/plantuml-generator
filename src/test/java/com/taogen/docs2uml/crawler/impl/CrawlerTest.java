package com.taogen.docs2uml.crawler.impl;

import com.taogen.docs2uml.entity.MyEntity;
import org.junit.Ignore;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Taogen
 */
@Ignore
public class CrawlerTest {

    protected void checkUrlOfMyEntities(List<MyEntity> myEntities) {
        for (MyEntity myEntity: myEntities){
            String url = myEntity.getUrl();
            assertNotNull(url);
            assertTrue(url.startsWith("http"));
        }
    }
}
