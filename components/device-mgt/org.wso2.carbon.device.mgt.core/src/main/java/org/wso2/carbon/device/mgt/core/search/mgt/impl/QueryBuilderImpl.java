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
import org.wso2.carbon.device.mgt.core.search.mgt.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryBuilderImpl implements QueryBuilder {


    private static final Log log = LogFactory.getLog(QueryBuilderImpl.class);
    private final String WILDCARD_OPERATOR = "%";
    private final String OR_OPERATOR = "OR";
    private String current_username;
    private boolean isDeviceAdminUser;

    @Override
    public Map<String, List<QueryHolder>> buildQueries(List<Condition> conditions) throws InvalidOperatorException {
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

        Map<String, List<QueryHolder>> queries = new HashMap<>();
        if ((!andColumns.isEmpty()) || (!orColumns.isEmpty())) {
            // Size is taken as the sum of both columns and for tenant id.
            ValueType valueTypeArray[] = new ValueType[andColumns.size() + orColumns.size() + 1];

//            String query =Utils.convertStringToList(

            // passing the integer value to the x so that array is correctly passed.
            Integer intArr[] = new Integer[1];
            intArr[0] = 1;
            //int x = 1;
            String query = this.getGenericQueryPart(valueTypeArray) +
                    this.processAND(andColumns, valueTypeArray,  intArr) +
                    this.processOR(orColumns, valueTypeArray,  intArr);
            List<QueryHolder> queryHolders = new ArrayList<>();
            QueryHolder queryHolder = new QueryHolder();
            queryHolder.setQuery(query);
            queryHolder.setTypes(valueTypeArray);
            queryHolders.add(queryHolder);

            queries.put(Constants.GENERAL, queryHolders);
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
    public String processAND(List<Condition> conditions, ValueType[] valueType, Integer intArr[]) throws InvalidOperatorException {
        String querySuffix = "";
        try {
            // TODO: find upto what address location of the array has filled.
            int x = intArr[0];
            for (Condition con : conditions) {
                if (Utils.checkDeviceDetailsColumns(con.getKey())) {
                    if (con.operator.equals(WILDCARD_OPERATOR)) {
                        querySuffix = querySuffix + " AND DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey())
                                + " LIKE  ? ";
                        ValueType type = new ValueType();
                        type.setColumnType(ValueType.columnType.STRING);
                        type.setStringValue("%"+con.getValue()+"%");
                        valueType[x] = type;
                        x++;
                    } else {
                        querySuffix = querySuffix + " AND DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey()) + con
                                .getOperator() + " ? ";
                        ValueType type = new ValueType();
                        if (Utils.checkColumnType(con.getKey())) {
                            type.setColumnType(ValueType.columnType.STRING);
                            type.setStringValue(con.getValue());
                        } else {
                            type.setColumnType(ValueType.columnType.INTEGER);
                            type.setIntValue(Integer.parseInt(con.getValue()));
                        }
                        valueType[x] = type;
                        x++;
                    }
                } else if (Utils.checkDeviceLocationColumns(con.getKey().toLowerCase())) {
                    querySuffix = querySuffix + " AND DL." + Utils.getDeviceLocationColumnNames().get(con.getKey().toLowerCase()) +
                            con.getOperator() + " ? ";
                    ValueType type = new ValueType();
                    type.setColumnType(ValueType.columnType.STRING);
                    type.setStringValue(con.getValue());
                    valueType[x] = type;
                    x++;
                }
            }
            intArr[0] = x;
        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the sql", e);
        }
        return querySuffix;
    }

    @Override
    public String processOR(List<Condition> conditions, ValueType[] valueType, Integer intArr[]) throws InvalidOperatorException {
        String querySuffix = "";
        // TODO: find upto what address location of the array has filled.
        try {
            int x = intArr[0];
            for (Condition con : conditions) {
                if (Utils.checkDeviceDetailsColumns(con.getKey())) {
                    if (con.operator.equals(WILDCARD_OPERATOR)) {
                        querySuffix = querySuffix + " OR DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey())
                                + " LIKE  ? ";
                        ValueType type = new ValueType();
                        type.setColumnType(ValueType.columnType.STRING);
                        type.setStringValue("%"+con.getValue()+"%");
                        valueType[x] = type;
                        x++;
                    } else {
                        querySuffix = querySuffix + " OR DD." + Utils.getDeviceDetailsColumnNames().get(con.getKey()) + con
                                .getOperator() + " ? ";

                        ValueType type = new ValueType();
                        if (Utils.checkColumnType(con.getKey())) {
                            type.setColumnType(ValueType.columnType.STRING);
                            type.setStringValue(con.getValue());
                        } else {
                            type.setColumnType(ValueType.columnType.INTEGER);
                            type.setIntValue(Integer.parseInt(con.getValue()));
                        }
                        valueType[x] = type;
                        x++;
                    }
                } else if (Utils.checkDeviceLocationColumns(con.getKey().toLowerCase())) {
                    querySuffix =
                            querySuffix + " OR DL." + Utils.getDeviceLocationColumnNames().get(con.getKey().toLowerCase())
                                    + con.getOperator() + " ? ";
                    ValueType type = new ValueType();
                    type.setColumnType(ValueType.columnType.STRING);
                    type.setStringValue(con.getValue());
                    valueType[x] = type;
                    x++;
                }
            }
            intArr[0] = x;
        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the sql", e);
        }
        if (!querySuffix.isEmpty()) {
            //Replacing the first OR operator as it's unnecessary
            querySuffix = querySuffix.replaceFirst(OR_OPERATOR, "");
            querySuffix = " AND (" + querySuffix + ")";
        }
        return querySuffix;
    }

    @Override
    public List<QueryHolder> processLocation(Condition condition) throws InvalidOperatorException {
        List<QueryHolder> queryHolders = new ArrayList<>();
        queryHolders.add(this.buildLocationQuery(condition.getValue()));
        return queryHolders;
    }

    @Override
    public List<QueryHolder> processANDProperties(List<Condition> conditions) throws InvalidOperatorException {
        return this.getQueryList(conditions);
    }

    @Override
    public List<QueryHolder> processORProperties(List<Condition> conditions) throws InvalidOperatorException {
        return this.getQueryList(conditions);
    }

    @Override
    public QueryHolder processUpdatedDevices(long epochTime) throws InvalidOperatorException {
        try {
            ValueType valueTypeArray[] = new ValueType[3];
            String query = this.getGenericQueryPart(valueTypeArray) + " AND ( DD.UPDATE_TIMESTAMP > ?  " +
                    "OR DL.UPDATE_TIMESTAMP >  ? )";

            ValueType val1 = new ValueType();
            val1.setColumnType(ValueType.columnType.LONG);
            val1.setLongValue(epochTime);
            valueTypeArray[1] = val1;

            ValueType val2 = new ValueType();
            val2.setColumnType(ValueType.columnType.LONG);
            val2.setLongValue(epochTime);
            valueTypeArray[2] = val2;

            QueryHolder queryHolder = new QueryHolder();
            queryHolder.setQuery(query);
            queryHolder.setTypes(valueTypeArray);

            return queryHolder;
        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the for the updated devices.", e);
        }
    }

    private List<QueryHolder> getQueryList(List<Condition> conditions) throws InvalidOperatorException {
        try {
            List<QueryHolder> queryHolders = new ArrayList<>();
            for (Condition con : conditions) {

                QueryHolder query = new QueryHolder();
                ValueType valueTypeArray[] = new ValueType[3];

                String querySuffix = this.getPropertyQueryPart(valueTypeArray) + " AND DI.KEY_FIELD = " + " ? " +
                        " AND DI.VALUE_FIELD " + con.getOperator() + " ? ";
                ValueType key = new ValueType();
                key.setColumnType(ValueType.columnType.STRING);
                key.setStringValue(con.getKey());
                valueTypeArray[1] = key;

                ValueType value = new ValueType();
                value.setColumnType(ValueType.columnType.STRING);
                value.setStringValue(con.getValue());
                valueTypeArray[2] = value;

                query.setQuery(querySuffix);
                query.setTypes(valueTypeArray);

                queryHolders.add(query);
            }
            return queryHolders;
        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the sql", e);
        }
    }

    private QueryHolder buildLocationQuery(String location) throws InvalidOperatorException {
        try {
            ValueType valueTypeArray[] = new ValueType[7];
            String query = this.getGenericQueryPart(valueTypeArray);
            query = query + " AND (DL.STREET1 LIKE ? ";
            query = query + " OR DL.STREET2 LIKE ? ";
            query = query + " OR DL.CITY LIKE ? ";
            query = query + " OR DL.STATE LIKE ? ";
            query = query + " OR DL.COUNTRY LIKE ? ";
            query = query + " OR DL.ZIP LIKE ? )";

            ValueType value = new ValueType();
            value.setColumnType(ValueType.columnType.STRING);
            value.setStringValue("%" + location + "%");

            // Same location is passed to each place
            valueTypeArray[1] = value;
            valueTypeArray[2] = value;
            valueTypeArray[3] = value;
            valueTypeArray[4] = value;
            valueTypeArray[5] = value;
            valueTypeArray[6] = value;

            QueryHolder queryHolder = new QueryHolder();
            queryHolder.setQuery(query);
            queryHolder.setTypes(valueTypeArray);

            return queryHolder;
        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the sql for location.", e);
        }
    }

    private String getGenericQueryPart(ValueType[] valueTypeArray) throws InvalidOperatorException {
        try {
            String query = "SELECT D.ID, D.DESCRIPTION, D.NAME,  \n" +
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
                    "WHERE D.TENANT_ID = ? ";

            ValueType type = new ValueType();
            type.setIntValue(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            type.setColumnType(ValueType.columnType.INTEGER);
            valueTypeArray[0] = type;
            return query;

        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the sql", e);
        }
    }

    private String getPropertyQueryPart(ValueType[] valueTypeArray) throws InvalidOperatorException {
        try {
            String query = "SELECT D.ID, D.DESCRIPTION, D.NAME,  \n" +
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
                    "WHERE D.TENANT_ID = ? ";

            ValueType type = new ValueType();
            type.setIntValue(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            type.setColumnType(ValueType.columnType.INTEGER);
            valueTypeArray[0] = type;
            return query;

        } catch (Exception e) {
            throw new InvalidOperatorException("Error occurred while building the sql", e);
        }
    }
}