/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.store.api.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.common.Comment;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.CommentManagementException;
import org.wso2.carbon.device.application.mgt.common.services.CommentsManager;
import org.wso2.carbon.device.application.mgt.store.api.APIUtil;
import org.wso2.carbon.device.application.mgt.store.api.services.impl.CommentManagementAPIImpl;
import org.wso2.carbon.device.application.mgt.store.api.services.util.CommentMgtTestHelper;

import javax.ws.rs.core.Response;

import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.ws.rs.*")
@SuppressStaticInitializationFor({
    "org.wso2.carbon.device.application.mgt.api.APIUtil" })
@PrepareForTest({ APIUtil.class, CommentsManager.class,
    CommentManagementAPITest.class})
public class CommentManagementAPITest {
    private static final Log log = LogFactory.getLog(CommentManagementAPI.class);

    private CommentManagementAPI commentManagementAPI;
    private CommentsManager commentsManager;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeClass
    void init() throws CommentManagementException {

        log.info("Initializing CommentManagementAPI tests");
        initMocks(this);
        this.commentsManager = Mockito.mock(CommentsManager.class, Mockito.RETURNS_DEFAULTS);
        this.commentManagementAPI = new CommentManagementAPIImpl();
    }

    @Test
    public void testGetAllCommentsWithValidDetails() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getAllComments("a", 1, 2);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
            "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetAllCommentsInternalError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.doThrow(new CommentManagementException()).when(this.commentsManager)
            .getAllComments(Mockito.any(), Mockito.anyString());
        Response response = this.commentManagementAPI.getAllComments("a", 1, 4);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetAllCommentsNotFoundError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getAllComments(null, 1, 3);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
            "The response status should be 404.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testAddComments() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.addComments(comment, "a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
            "The response status should be 201.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testAddNullComment() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.addComments(null, "a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
            "The response status should be 400.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testAddCommentsInternalError() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.when(this.commentManagementAPI.addComments(Mockito.any(), Mockito.anyString()))
            .thenThrow(new CommentManagementException());
        Response response = this.commentManagementAPI.addComments(comment, null);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testUpdateComment() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateComment(comment, 1);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
            "The response status should be 200.");
    }

    @Test
    public void testUpdateNullComment() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateComment(null, 1);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
            "The response status should be 400.");
    }

    @Test
    public void testUpdateCommentWhenNullCommentId() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateComment(comment, 0);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
            "The response status should be 404.");
    }

    @Test
    public void testUpdateCommentInternalServerError() throws Exception {
        Comment comment = CommentMgtTestHelper.getDummyComment("a", "a");
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.doThrow(new CommentManagementException()).when(this.commentsManager).updateComment(comment, 9);
        Response response = this.commentManagementAPI.updateComment(comment, 9);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
    }

    @Test
    public void testDeleteComment() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.deleteComment(1);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
            "The response status should be 200.");
    }

    @Test
    public void testDeleteCommentInternalError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.when(this.commentManagementAPI.deleteComment(1)).thenThrow(new CommentManagementException());
        Response response = this.commentManagementAPI.deleteComment(1);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
    }

    @Test
    public void testDeleteCommentNotFoundError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.deleteComment(0);
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode(),
            "The response status should be 404.");
    }

    @Test
    public void testGetStars() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getStars("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
            "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetStarsCommentError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.when(this.commentManagementAPI.getStars(Mockito.anyString()))
            .thenThrow(new CommentManagementException());
        Response response = this.commentManagementAPI.getStars("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetStarsApplicationError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.when(this.commentManagementAPI.getStars(Mockito.anyString()))
            .thenThrow(new ApplicationManagementException());
        Response response = this.commentManagementAPI.getStars("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetRatedUser() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.getRatedUser("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
            "The response status should be 200.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetRatedUserCommentError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.when(this.commentManagementAPI.getRatedUser(Mockito.anyString()))
            .thenThrow(new CommentManagementException());
        Response response = this.commentManagementAPI.getRatedUser("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testGetRatedUserApplicationError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.when(this.commentManagementAPI.getRatedUser(Mockito.anyString()))
            .thenThrow(new ApplicationManagementException());
        Response response = this.commentManagementAPI.getRatedUser("a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testUpdateStars() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateStars(3, "a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.CREATED.getStatusCode(),
            "The response status should be 201.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testUpdateInvalidStars() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Response response = this.commentManagementAPI.updateStars(0, "a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode(),
            "The response status should be 400.");
        Mockito.reset(commentsManager);
    }

    @Test
    public void testUpdateStarsApplicationError() throws Exception {
        PowerMockito.stub(PowerMockito.method(APIUtil.class, "getCommentsManager")).toReturn(this.commentsManager);
        Mockito.doThrow(new ApplicationManagementException()).when(this.commentsManager)
            .updateStars(Mockito.anyInt(), Mockito.anyString());
        Response response = this.commentManagementAPI.updateStars(3, "a");
        Assert.assertNotNull(response, "The response object is null.");
        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "The response status should be 500.");
        Mockito.reset(commentsManager);
    }
}