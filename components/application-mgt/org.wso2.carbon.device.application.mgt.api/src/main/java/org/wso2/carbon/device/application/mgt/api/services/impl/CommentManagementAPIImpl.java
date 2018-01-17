package org.wso2.carbon.device.application.mgt.api.services.impl;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.application.mgt.api.APIUtil;
import org.wso2.carbon.device.application.mgt.api.services.CommentManagementAPI;
import org.wso2.carbon.device.application.mgt.common.Comment;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.CommentManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.services.CommentsManager;

import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment Management related jax-rs APIs.
 */
@Path("/comments")
@SuppressWarnings( "deprecation" )
public class CommentManagementAPIImpl implements CommentManagementAPI{

    private static Log log = LogFactory.getLog(CommentManagementAPIImpl.class);

    @Override
    @GET
    @Path("/{uuid}")
    public Response getAllComments(
            @PathParam("uuid") String uuid,
            @QueryParam("offset")int offSet,
            @QueryParam("limit")int limit){

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        List<Comment> comments = new ArrayList<>();
        try {
            PaginationRequest request=new PaginationRequest(offSet,limit);
            if(request.validatePaginationRequest(offSet,limit)) {
                commentsManager.getAllComments(request, uuid);
                return Response.status(Response.Status.OK).entity(comments).build();
            }
        } catch (NotFoundException e){
            log.error("Not found exception occurs to uuid "+uuid+" .",e);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Application with UUID " + uuid + " not found").build();
        }
        catch (CommentManagementException e) {
            String msg = "Error occurred while retrieving comments.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (SQLException e) {
            log.error("SQL Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(comments).build();
    }

    @Override
    @POST
    @Consumes("application/json")
    @Path("/{uuid}")
    public Response addComments(
            @ApiParam Comment comment,
            @PathParam("uuid") String uuid){

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int tenantId= PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            Comment newComment = commentsManager.addComment(comment,uuid,tenantId);
            if (comment != null){
                return Response.status(Response.Status.CREATED).entity(newComment).build();
            }else{
                String msg = "Given comment is not valid ";
                log.error(msg);
                return  Response.status(Response.Status.BAD_REQUEST).build();
            }
        }catch (CommentManagementException e) {
            String msg = "Error occurred while creating the comment";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @PUT
    @Consumes("application/json")
    @Path("/{apAppCommentId}")
    public Response updateComment(
            @ApiParam Comment comment,
            @PathParam("apAppCommentId") int apAppCommentId) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();

        try {
            if (apAppCommentId == 0) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Comment with comment id " + apAppCommentId + " not found").build();
            }else if(comment==null){
                String msg = "Given comment is not valid ";
                log.error(msg);
                return  Response.status(Response.Status.BAD_REQUEST).build();
            } else{
                comment = commentsManager.updateComment(comment,apAppCommentId);
                return Response.status(Response.Status.OK).entity(comment).build();
            }
        } catch (CommentManagementException e) {
            String msg = "Error occurred while retrieving comments.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (SQLException e) {
            log.error("SQL Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }catch (DBConnectionException e) {
            log.error("DB Connection Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @DELETE
    @Path("/{apAppCommentId}")
    public Response deleteComment(
            @PathParam("apAppCommentId")
                    int apAppCommentId){

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        try {
            commentsManager.deleteComment(apAppCommentId);
        } catch (CommentManagementException e) {
            String msg = "Error occurred while deleting the comment.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (NotFoundException e) {
            log.error("Not found exception occurs to comment id "+apAppCommentId+" .",e);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Comment with" + apAppCommentId + " not found").build();
        }
        return Response.status(Response.Status.OK).entity("Comment is deleted successfully.").build();
    }

    @Override
    @GET
    @Path("/{uuid}")
    public Response getStars(
            @PathParam("uuid")
                    String uuid) {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int Stars=0;

        try {
            Stars= commentsManager.getStars(uuid);
        }  catch (SQLException e) {
            log.error("SQL Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(Stars).build();
    }

    @Override
    @GET
    @Path("/{uuid}")
    public Response getRatedUser(
            @PathParam("uuid")
            String uuid){

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int ratedUsers=0;

        try {
            ratedUsers= commentsManager.getRatedUser(uuid);
        } catch (SQLException e) {
            log.error("SQL Exception occurs", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.OK).entity(ratedUsers).build();
    }

    @Override
    @POST
    @Consumes("uuid/stars/json")
    public Response updateStars(
            @ApiParam int stars,
            @PathParam("uuid") String uuid) throws SQLException {

        CommentsManager commentsManager = APIUtil.getCommentsManager();
        int newStars=commentsManager.getStars(uuid);

        try {
            newStars = commentsManager.updateStars(stars,uuid);

            if (stars != 0){
                return Response.status(Response.Status.CREATED).entity(newStars).build();
            }else{
                String msg = "Given star value is not valid ";
                log.error(msg);
                return  Response.status(Response.Status.BAD_REQUEST).build();
            }

        } catch (ApplicationManagementException e) {
            log.error("Application Management Exception occurs", e);
        }
        return Response.status(Response.Status.OK).entity(newStars).build();
    }
}