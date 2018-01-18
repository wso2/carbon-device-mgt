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
package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.Comment;
import org.wso2.carbon.device.application.mgt.common.PaginationRequest;
import org.wso2.carbon.device.application.mgt.common.PaginationResult;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.CommentManagementException;

import java.util.List;

/**
 * CommentsManager is responsible for handling all the add/update/delete/get operations related with
 */
public interface CommentsManager {

    /**
     * To add a comment to a application
     *
     * @param comment  comment of the application.
     * @param uuid     uuid of the application release
     * @param tenantId tenant id of the application
     * @return {@link Comment} Comment added
     * @throws CommentManagementException Exceptions of the comment management.
     */
    Comment addComment(Comment comment, String uuid, int tenantId) throws CommentManagementException;

    /**
     * To validate the pre-request of the comment
     *
     * @param commentId ID of the comment.
     * @param comment   comment needed to be validate.
     * @return validated the comment.
     * @throws CommentManagementException Exceptions of the comment management.
     */
    Boolean validateComment(int commentId, String comment) throws CommentManagementException;

    /**
     * Get all comments to pagination
     *
     * @param request Pagination request
     * @param uuid    uuid of the application release
     * @return {@link PaginationResult} pagination result with starting offSet and limit
     * @throws CommentManagementException Exceptions of the comment management.
     */
    List<Comment> getAllComments(PaginationRequest request, String uuid) throws CommentManagementException;

    /**
     * To get the comment with id.
     *
     * @param commentId id of the comment
     * @return {@link Comment}Comment of the comment id
     * @throws CommentManagementException Exceptions of the comment management.
     */
    Comment getComment(int commentId) throws CommentManagementException;

    /**
     * To delete comment using comment id.
     *
     * @param commentId id of the comment
     * @throws CommentManagementException Exceptions of the comment management
     */
    void deleteComment(int commentId) throws CommentManagementException;

    /**
     * To update a comment.
     *
     * @param comment   comment of the application.
     * @param commentId id of the comment
     * @return {@link Comment}updated comment
     * @throws CommentManagementException Exceptions of the comment management
     */
    Comment updateComment(Comment comment, int commentId) throws CommentManagementException;

    /**
     * To get number of rated users
     *
     * @param uuid uuid of the application
     * @return number of rated users
     * @throws CommentManagementException     Exceptions of the comment management
     * @throws ApplicationManagementException Application Management Exception.
     */
    int getRatedUser(String uuid) throws CommentManagementException, ApplicationManagementException;

    /**
     * To get the average of stars
     *
     * @param uuid uuid of the comment
     * @return value of the stars of an application
     * @throws CommentManagementException     Exceptions of the comment management
     * @throws ApplicationManagementException Application Management Exception.
     */
    int getStars(String uuid) throws CommentManagementException, ApplicationManagementException;

    /**
     * To update rating stars
     *
     * @param stars amount of stars
     * @param uuid  uuid of the application
     * @return value of the added stars
     * @throws ApplicationManagementException Application Management Exception.
     */
    int updateStars(int stars, String uuid) throws ApplicationManagementException;
}