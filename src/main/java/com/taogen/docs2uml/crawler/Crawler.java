package com.taogen.docs2uml.crawler;

import com.taogen.docs2uml.entity.MyEntity;

import java.util.List;

/**
 * @author Taogen
 */
public interface Crawler {
    /**
     * crawling information from Web page
     * @return crawling result
     */
    List<MyEntity> crawl();
}
