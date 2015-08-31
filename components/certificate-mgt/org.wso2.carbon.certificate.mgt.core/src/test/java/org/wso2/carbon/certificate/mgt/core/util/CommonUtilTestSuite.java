package org.wso2.carbon.certificate.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;

public class CommonUtilTestSuite {

    private static Log log = LogFactory.getLog(CommonUtilTestSuite.class);
    private final CommonUtil commonUtil = new CommonUtil();

    @Test
    public void testValidityStartDate() {
        Date validityStartDate = commonUtil.getValidityStartDate();

        if(validityStartDate == null) {
            Assert.fail("Validity start date is empty");
        }

        Date todayDate = new Date();
        Assert.assertTrue(validityStartDate.before(todayDate), "Validity start date is valid");
    }

    @Test
    public void testValidityEndDate() {
        Date validityEndDate = commonUtil.getValidityEndDate();

        if(validityEndDate == null) {
            Assert.fail("Validity end date is empty");
        }

        Date todayDate = new Date();
        Assert.assertTrue(validityEndDate.after(todayDate), "Validity end date is valid");
    }
}
