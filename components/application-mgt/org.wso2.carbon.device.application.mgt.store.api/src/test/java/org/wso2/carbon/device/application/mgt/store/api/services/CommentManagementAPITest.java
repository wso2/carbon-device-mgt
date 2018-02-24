package org.wso2.carbon.device.application.mgt.store.api.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.common.Comment;
import org.wso2.carbon.device.application.mgt.common.exception.CommentManagementException;
import org.wso2.carbon.device.application.mgt.common.services.CommentsManager;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.impl.CommentManagementAPIImpl;
import org.wso2.carbon.device.application.mgt.store.api.services.util.CommentMgtTestHelper;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.ws.rs.*") @SuppressStaticInitializationFor({
        "org.wso2.carbon.device.application.mgt.api.APIUtil" }) @PrepareForTest({ APIUtil.class, CommentsManager.class,
        CommentManagementAPITest.class, MultitenantUtils.class }) public class CommentManagementAPITest {
    private static final Log log = LogFactory.getLog(CommentManagementAPI.class);

    private CommentManagementAPI commentManagementAPI;
    private CommentsManager commentsManager;
    private int tenantId;

    @ObjectFactory public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass void init() throws CommentManagementException {

        log.info("Initializing CommentManagementAPI tests");
        initMocks(this);
        this.commentsManager = Mockito.mock(CommentsManager.class, Mockito.RETURNS_DEFAULTS);
        this.commentManagementAPI = new CommentManagementAPIImpl();
    }

    @Test public void testGetAllCommentsWithValidDetails() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getAllComments("a", 1, 2);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test public void testAddComments() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        //        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
        //                .toReturn(Mockito.mock(PrivilegedCarbonContext.class, Mockito.RETURNS_MOCKS));
        //        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getTenantId"))
        //                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        Response response = this.commentManagementAPI.addComments(comment, null);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
                "The response status should be 201.");
        Mockito.reset(commentsManager);
    }
    @Test public void testAddNullComment() throws Exception {
        Comment comment=null;
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        //        PowerMockito.stub(PowerMockito.method(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext"))
        //                .toReturn(Mockito.mock(PrivilegedCarbonContext.class, Mockito.RETURNS_MOCKS));
        //        PowerMockito.stub(PowerMockito.method(CarbonContext.class, "getTenantId"))
        //                .toReturn(Mockito.mock(CarbonContext.class, Mockito.RETURNS_MOCKS));
        Response response = this.commentManagementAPI.addComments(comment, null);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400.");
        Mockito.reset(commentsManager);
    }

    @Test public void testUpdateComment() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateComment(comment, 1);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200.");
    }
    @Test public void testUpdateNullComment() throws Exception {
        Comment comment=null;
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateComment(comment, 1);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400.");
    }
    @Test public void testUpdateCommentWhenNullCommentId() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateComment(comment, 0);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
                "The response status should be 404.");
    }

    @Test public void testGetStars() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getStars("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test public void testGetRatedUser() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getRatedUser("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test public void testUpdateStars() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateStars(3,"a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
                "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test public void testUpdateInvalideStars() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateStars(0,"a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
                "The response status should be 400.");
        Mockito.reset(commentsManager);
    }

}