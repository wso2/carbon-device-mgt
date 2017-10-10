/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.Category;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.util.List;

/**
 * This is responsible for Application Category related DAO operations.
 */
public interface CategoryDAO {

    /**
     * To add a new category.
     *
     * @param category Category that need to be added.
     * @return Newly added category.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Category addCategory(Category category) throws ApplicationManagementDAOException;

    /**
     * To get the existing categories.
     *
     * @return Existing categories.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    List<Category> getCategories() throws ApplicationManagementDAOException;

    /**
     * To get the category with the given name.
     *
     * @param name Name of the Application category.
     * @return Application Category.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    Category getCategory(String name) throws ApplicationManagementDAOException;

    /**
     * To delete a particular category.
     *
     * @param name Name of the category that need to be deleted.
     * @throws ApplicationManagementDAOException Application Management DAO Exception.
     */
    void deleteCategory(String name) throws ApplicationManagementDAOException;
}
