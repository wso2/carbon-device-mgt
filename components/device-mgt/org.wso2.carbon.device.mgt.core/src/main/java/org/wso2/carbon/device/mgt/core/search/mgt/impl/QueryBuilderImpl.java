/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.search.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.search.Condition;
import org.wso2.carbon.device.mgt.core.search.mgt.Constants;
import org.wso2.carbon.device.mgt.core.search.mgt.InvalidOperatorException;
import org.wso2.carbon.device.mgt.core.search.mgt.QueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilderImpl implements QueryBuilder {


    private static final Log log = LogFactory.getLog(QueryBuilderImpl.class);
    private final String WILDCARD_OPERATOR = "%";

    @Override
    public Map<String, List<String>> buildQueries(List<Condition> conditions) throws InvalidOperatorException {

        List<Condition> andColumns = new ArrayList<>();
        List<Condition> orColumns = new ArrayList<>();
        List<Condition> otherANDColumns = new ArrayList<>();
        List<Condition> otherORColumns = new ArrayList<>();
        Condition locCondition = new Condition();

        if (conditions.size() == 1) {
            if (Constants.LOCATION.equalsIgnoreCase(conditions.get(0).getKey())) {
                locCondition = conditions.get(0);
            } else if (Utils.checkDeviceDetailsColumns(conditions.get(0).getKey()) ||
                    Utils.checkDeviceLocationColumns(conditions.get(0).getKey())) {
                andColumns.add(conditions.get(0));
            } else {
                otherANDColumns.add(conditions.get(0));
            }
        } else {
            for (Condition con : conditions) {
                if (Constants.LOCATION.equalsIgnoreCase(con.getKey())) {
                    locCondition = con;
                } else if (Utils.checkDeviceDetailsColumns(con.getKey()) ||
                        Utils.checkDeviceLocationColumns(con.getKey())) {
                    if (con.getState().equals(Condition.State.AND)) {
                        andColumns.add(con);
                    } else if (con.getState().equals(Condition.State.OR)) {
                        orColumns.add(con);
                    } else {
                        throw new InvalidOperatorException(con.getState() + " is not a valid operator.");
                    }
                } else {
                    if (con.getState().equals(Condition.State.AND)) {
                        otherANDColumns.add(con);
                    } else if (con.getState().equals(Condition.State.OR)) {
                        otherORColumns.add(con);
                    } else {
                        throw new InvalidOperatorException(con.getState() + " is not a valid operator.");
                    }
                }
            }
        }

        Map<String, List<String>> queries = new HashMap<>();
        if ((!andColumns.isEmpty()) || (!orColumns.isEmpty())) {
            queries.put(Constants.GENERAL, Utils.convertStringToList(this.getGenericQueryPart() + this.processAND(andColumns) +
                    this.processOR(orColumns)));
        }
        if (!otherANDColumns.isEmpty()) {
            queries.put(Constants.PROP_AND, this.processANDProperties(otherANDColumns));
        }
        if (!otherORColumns.isEmpty()) {
            queries.put(Constants.PROP_OR, this.processORProperties(otherORColumns));
        }
        if (locCondition != null && locCondition.getValue() != null) {
            queries.put(Constants.LOCATION, this.processLocation(locCondition));
        }

        if (log.isDebugEnabled()) {
            log.debug("General Query : " + queries.get(Constants.GENERAL));
            log.debug("Property with AND Query : " + queries.get(Constants.PROP_AND));
            log.debug("Property with OR Query : " + queries.get(Constants.PROP_OR));
            log.debug("Location related Query : " + queries.get(Constants.LOCATION));
        }

        return queries;
    }

    @Override
    public String processAND(List<Condition> conditions) throws InvalidOperatorException {
        String querySuffix = "";
        for (Condition con : conditions) {
            if (Utils.checkDeviceDetailsColumns(con.getKey())) {
                if (con.operator.equals(WILDCARD_OPERATOR)){
                    querySuffix = querySuffix + " AND DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey())
                            + " LIKE \'" + con.operator + Utils.getConvertedValue(con.getKey(), con.getValue())
                            + con.operator + "\'";
                } else {
                    querySuffix = querySuffix + " AND DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey()) + con
                            .getOperator() + Utils.getConvertedValue(con.getKey(), con.getValue());
                }
            } else if (Utils.checkDeviceLocationColumns(con.getKey().toLowerCase())) {
                querySuffix = querySuffix + " AND DL." + Utils.getDeviceLocationColumnNames().get(con.getKey().toLowerCase()) +
                        con.getOperator() + con.getValue();
            }
        }
        return querySuffix;
    }

    @Override
    public String processOR(List<Condition> conditions) throws InvalidOperatorException {
        String querySuffix = "";
        for (Condition con : conditions) {
            if (Utils.checkDeviceDetailsColumns(con.getKey())) {
                if (con.operator.equals(WILDCARD_OPERATOR)) {
                    querySuffix = querySuffix + " OR DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey())
                                + " LIKE \'" + con.operator + Utils.getConvertedValue(con.getKey(), con.getValue())
                                + con.operator + "\'";
                } else {
                    querySuffix = querySuffix + " OR DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey()) + con
                            .getOperator() + Utils.getConvertedValue(con.getKey(), con.getValue());
                }
            } else if (Utils.checkDeviceLocationColumns(con.getKey().toLowerCase())) {
                querySuffix =
                        querySuffix + " OR DL." + Utils.getDeviceLocationColumnNames().get(con.getKey().toLowerCase())
                                + con.getOperator() + con.getValue();
            }
        }
        return querySuffix;
    }

    @Override
    public List<String> processLocation(Condition condition) throws InvalidOperatorException {
        List<String> queryList = new ArrayList<>();
        queryList.add(this.buildLocationQuery(condition.getValue()));
        return queryList;
    }

    @Override
    public List<String> processANDProperties(List<Condition> conditions) throws InvalidOperatorException {
        return this.getQueryList(conditions);
    }

    @Override
    public List<String> processORProperties(List<Condition> conditions) throws InvalidOperatorException {
        return this.getQueryList(conditions);
    }

    @Override
    public String processUpdatedDevices(long epochTime) throws InvalidOperatorException {
        return this.getGenericQueryPart() + " AND ( DD.UPDATE_TIMESTAMP > " + epochTime +
                " OR DL.UPDATE_TIMESTAMP > " + epochTime + " )";
    }

    private List<String> getQueryList(List<Condition> conditions) {
        List<String> queryList = new ArrayList<>();
        for (Condition con : conditions) {

            String querySuffix = this.getPropertyQueryPart() + " AND DI.KEY_FIELD = " + "\'" + con.getKey() + "\'" +
                    " AND DI.VALUE_FIELD " + con.getOperator() + "\'" + con.getValue() + "\'";
            queryList.add(querySuffix);
        }
        return queryList;
    }

    private String buildLocationQuery(String location) {

        String query = this.getGenericQueryPart();
        query = query + " AND DL.STREET1 LIKE \'%" + location + "%\'";
        query = query + " OR DL.STREET2 LIKE \'%" + location + "%\'";
        query = query + " OR DL.CITY LIKE \'%" + location + "%\'";
        query = query + " OR DL.STATE LIKE \'%" + location + "%\'";
        query = query + " OR DL.COUNTRY LIKE \'%" + location + "%\'";
        query = query + " OR DL.ZIP LIKE \'%" + location + "%\'";
        return query;
    }

    private String getGenericQueryPart() {
        return "SELECT D.ID, D.DESCRIPTION, D.NAME,  \n" +
                "D.DEVICE_TYPE_ID, D.DEVICE_IDENTIFICATION,  DT.ID AS DEVICE_TYPE_ID, \n" +
                "DT.NAME AS DEVICE_TYPE_NAME, DD.DEVICE_ID, DD.DEVICE_MODEL, DD.VENDOR, \n" +
                "DD.OS_VERSION, DD.OS_BUILD_DATE, DD.BATTERY_LEVEL, DD.INTERNAL_TOTAL_MEMORY, DD.INTERNAL_AVAILABLE_MEMORY,\n" +
                "DD.EXTERNAL_TOTAL_MEMORY, DD.EXTERNAL_AVAILABLE_MEMORY, DD.CONNECTION_TYPE, \n" +
                "DD.SSID, DD.CPU_USAGE, DD.TOTAL_RAM_MEMORY, DD.AVAILABLE_RAM_MEMORY, \n" +
                "DD.PLUGGED_IN, DD.UPDATE_TIMESTAMP, DL.LATITUDE, DL.LONGITUDE, DL.STREET1, DL.STREET2, DL.CITY, DL.ZIP, \n" +
                "DL.STATE, DL.COUNTRY, DL.UPDATE_TIMESTAMP AS DL_UPDATED_TIMESTAMP, DE.OWNER, DE.OWNERSHIP, DE.STATUS " +
                "AS DE_STATUS FROM DM_DEVICE_DETAIL AS DD INNER JOIN DM_DEVICE AS D ON  D.ID=DD.DEVICE_ID\n" +
                "LEFT JOIN DM_DEVICE_LOCATION AS DL ON DL.DEVICE_ID=D.ID \n" +
                "INNER JOIN DM_DEVICE_TYPE AS DT ON DT.ID=D.DEVICE_TYPE_ID\n" +
                "INNER JOIN DM_ENROLMENT AS DE ON D.ID=DE.DEVICE_ID\n" +
                "WHERE D.TENANT_ID = " +
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private String getPropertyQueryPart() {
        return "SELECT D.ID, D.DESCRIPTION, D.NAME,  \n" +
                "D.DEVICE_TYPE_ID, D.DEVICE_IDENTIFICATION,  DT.ID AS DEVICE_TYPE_ID, \n" +
                "DT.NAME AS DEVICE_TYPE_NAME, DD.DEVICE_ID, DD.DEVICE_MODEL, DD.VENDOR, \n" +
                "DD.OS_VERSION, DD.OS_BUILD_DATE, DD.BATTERY_LEVEL, DD.INTERNAL_TOTAL_MEMORY, DD.INTERNAL_AVAILABLE_MEMORY,\n" +
                "DD.EXTERNAL_TOTAL_MEMORY, DD.EXTERNAL_AVAILABLE_MEMORY, DD.CONNECTION_TYPE, \n" +
                "DD.SSID, DD.CPU_USAGE, DD.TOTAL_RAM_MEMORY, DD.AVAILABLE_RAM_MEMORY, \n" +
                "DD.PLUGGED_IN, DD.UPDATE_TIMESTAMP, DL.LATITUDE, DL.LONGITUDE, DL.STREET1, DL.STREET2, DL.CITY, DL.ZIP, \n" +
                "DL.STATE, DL.COUNTRY, DL.UPDATE_TIMESTAMP AS DL_UPDATED_TIMESTAMP, DI.KEY_FIELD, DI.VALUE_FIELD, \n" +
                "DE.OWNER, DE.OWNERSHIP, DE.STATUS AS DE_STATUS " +
                "FROM DM_DEVICE_DETAIL AS DD INNER JOIN DM_DEVICE AS D ON  D.ID=DD.DEVICE_ID\n" +
                "LEFT JOIN DM_DEVICE_LOCATION AS DL ON DL.DEVICE_ID=D.ID  \n" +
                "INNER JOIN DM_DEVICE_TYPE AS DT ON DT.ID=D.DEVICE_TYPE_ID\n" +
                "INNER JOIN DM_ENROLMENT AS DE ON D.ID=DE.DEVICE_ID\n" +
                "LEFT JOIN DM_DEVICE_INFO AS DI ON DI.DEVICE_ID=D.ID\n" +
                "WHERE D.TENANT_ID = " +
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}
