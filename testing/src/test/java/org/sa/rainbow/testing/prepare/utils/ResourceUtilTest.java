package org.sa.rainbow.testing.prepare.utils;

import org.junit.Test;

import java.io.FileNotFoundException;

public class ResourceUtilTest {

    @Test(expected = FileNotFoundException.class)
    public void extractResourceNotFound() throws Exception {
        ResourceUtil.extractResource("not-found.none");
    }
}
