package com.bouyguesenergiesservices.opcuafctaddon.client;


import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;
import com.inductiveautomation.ignition.client.gateway_interface.FilteredPushNotificationListener;
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.execution.impl.BasicExecutionEngine;
import com.inductiveautomation.ignition.common.gateway.messages.PushNotification;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.builtin.PyArgumentMap;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.parser.TagPathParser;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;


/**
 * Convention designation of the columnName in Dataset to be manage by this Module
 *
 * Sample columnName in Dataset:
 *  OPC_X.TagFullPath - OPC = NAME_COLUMN_PK , X = Your GroupColumnName, TagFullPath = Type of this column : a full path OPC
 *  OPC_X.Value - OPC = NAME_COLUMN_PK , X = Your GroupColumnName, Value = Type of this column : Value associate to the Column OPC_X.TagFullPath find in dataset
 *  TOTO - This column isn't manage by this Module, just pass by the client to keep a workable dataset in result
 *
 * @version
 *
 * Created by regis on 18/10/2016.
 */
public class ClientFct {

    // the dataset client tag to create in ignition to be notified of tags data subscriptions
    private static final String TAGPATH_CLIENT_DATASET = "[Client]opcua_subscription";
    private static final Integer UPDATE_FREQUENCY_MS = 250;
    private static final String EXECUTION_ENGINE_NAME = "CyclicUpdateTagClientDatasetFromDataset";

    private static final String NAME_COLUMN_PK = "OPC";
    private static final String NAME_COLUMN_TAG_PATH = "TagFullPath";

    //regex init to find name column
    private static final String REGEX_NAME_COLUMN_PROPERTY = String.format("%s_(.*?)(\\.Quality|\\.Value|\\.LastChange)",NAME_COLUMN_PK);
    private static final String REGEX_NAME_COLUMN_TAGPATH = String.format("%s_(.*?)(\\.TagFullPath)",NAME_COLUMN_PK);


    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IGatewayFctRPCBase myGatewayRpc;
    private final ClientContext clientContext;


    //use for register Timer callBack
    private AtomicBoolean updateTagClientFlag = new AtomicBoolean(false);
    private BasicExecutionEngine executionEngine = null;
    private ExecutionCyclic executionCyclic = null;

    private boolean flagRegistered = false;


    // Dataset elements
    private BasicDataset dataset = null;
    private List<Class<?>> listColumnTypes = null;
    private HashMap<String,GroupColumnsInfo> listGroupManage = new HashMap<>(); // Key: Name Column


    private List<String> listTagPath = new ArrayList<>();
    private List<String> listTagColumName = new ArrayList<>();
    private List<Integer> listTagRowIndex = new ArrayList<>();

    private BasicDataset freezeDataset = null;
    private HashMap<String,GroupColumnsInfo> freezeListGroupManage = new HashMap<>(); // Key: Name Column


    /**
     * Group Columns Informations manage by the this module
     * (Your groupColumn name, index of all associated columns (TagFullPath,Value,Quality,LastChange)
     */
    private class GroupColumnsInfo{

        private final String REGEX_NAME_COLUMN_VALUE = String.format("%s_(.*?)(\\.Value)",NAME_COLUMN_PK);
        private final String REGEX_NAME_COLUMN_QUALITY = String.format("%s_(.*?)(\\.Quality)",NAME_COLUMN_PK);
        private final String REGEX_NAME_COLUMN_TIMESTAMP = String.format("%s_(.*?)(\\.LastChange)",NAME_COLUMN_PK);


        public final String columnName;
        public final int columnIndex;

        public List<Integer> valueColumnIndex = null; //Columns Index in DS where the .Value is manage
        public List<Integer> qualityColumnIndex = null;//Columns Index in DS where the .Quality is manage
        public List<Integer> timeStampColumnIndex = null;//Columns Index in DS where the .LastChange is manage


        public GroupColumnsInfo(String _columnName,int _columnIndex){
            this.columnName = _columnName;
            this.columnIndex= _columnIndex;
        }


        /**
         * Add a new manage Column found in the Dataset pass
         *
         * @param column Name of the column
         * @param index Index of the column
         * @param columnType Type of column associate to this column
         * @return Type of column associate to this column format
         */
        public Class<?> addElt(String column, int index, Class<?> columnType){
            if (column.matches(REGEX_NAME_COLUMN_VALUE)){
                if (valueColumnIndex == null){
                    valueColumnIndex = new ArrayList<>();
                }
                valueColumnIndex.add(index);
                columnType = String.class;

            }else if (column.matches(REGEX_NAME_COLUMN_QUALITY)) {
                if (qualityColumnIndex == null){
                    qualityColumnIndex = new ArrayList<>();
                }
                qualityColumnIndex.add(index);
                columnType = String.class;
            }else if (column.matches(REGEX_NAME_COLUMN_TIMESTAMP)){
                if (timeStampColumnIndex == null){
                    timeStampColumnIndex = new ArrayList<>();
                }
                timeStampColumnIndex.add(index);
                columnType =  java.util.Date.class;
            }
            return columnType;
        }

        /**
         * Set a newValue at the specific row in the Dataset
         *
         * @param newValue New qualifiedValue
         * @param row Index of the row to update
         */
        public void setValueAtDataset(QualifiedValue newValue,  int row) {
            if (newValue != null){
                setMyValueAt(valueColumnIndex, newValue.getValue().toString(), row);
                setMyValueAt(qualityColumnIndex, newValue.getQuality().getDescription().toString(), row);
                setMyValueAt(timeStampColumnIndex, newValue.getTimestamp(), row);
            }

        }

        /**
         * Set value at the specific row and foreach column of this type
         *
         * @param propertyColumnIndex List of index for the protertyColumn
         * @param value New Value
         * @param row Index of the row to update
         */
        private void setMyValueAt(List<Integer> propertyColumnIndex, Object value, int row){
            if (propertyColumnIndex!=null){
                propertyColumnIndex.forEach(col -> {
                    logger.trace("setMyValueAt() >  index row:[{}] index col:[{}] value:[{}]", row,col,value);
                    dataset.setValueAt(row, col, value);
                });

            }
        }

        @Override
        public String toString(){
            return String.format("%s:%d value:[%s] quality:[%s] timestamp:[%s]",
                    columnName,
                    columnIndex,
                    valueColumnIndex.toString(),
                    qualityColumnIndex.toString(),
                    timeStampColumnIndex.toString());
        }

    }

    /**
     *  Initialize an RPC proxies for communication between the client and the gateway.
     * GetOPCRPC Interface, it will generate a class that will forward calls to the module's RPC handler in the gateway, which should implement the interface.
     *
     * @param clientContext context of this client
     */
    public ClientFct(ClientContext clientContext){

        this.clientContext = clientContext;
        this.myGatewayRpc = ModuleRPCFactory.create("com.bouyguesenergiesservices.OpcUaFctAddon", IGatewayFctRPCBase.class);
        //register my Notification Listener to the Gateway
        GatewayConnectionManager.getInstance().addPushNotificationListener(new MyGatewayNotificationListener("com.bouyguesenergiesservices.OpcUaFctAddon","TagChanged"));

    }

    /**
     * Search all managed column in the dataset
     *
     * @param dataset Scan this new dataset
     * @return At least one groupColumn was found
     */
    private boolean searchAllManagedCol(Dataset dataset){

        boolean result = false;
        if (dataset == null){
            logger.error("Dataset is null");
        } else {
            List<String> columnNames = dataset.getColumnNames();

            //erase previous group of Column managed
            listGroupManage.clear();
            listColumnTypes = new ArrayList<Class<?>>(dataset.getColumnTypes());

           //Search all PK REGEX_NAME_COLUMN_TAGPATH and extract only name and index for the groupName
            IntStream.range(0,columnNames.size())
                    .forEach(index-> {

                Pattern pattern = Pattern.compile(REGEX_NAME_COLUMN_TAGPATH);
                Matcher matcher = pattern.matcher(columnNames.get(index));

                if (matcher.find()) {
                    logger.debug("searchAllManagedCol()> This column [{}] is a REGEX_NAME_COLUMN_TAGPATH", matcher.group(1));
                    listGroupManage.put(matcher.group(1), new GroupColumnsInfo(matcher.group(1), index));
                }
            });

            //Search each supported name column in DS ColumnNames for all group
            IntStream.range(0,columnNames.size())
                    .forEach(index-> {
                        Pattern pattern = Pattern.compile(REGEX_NAME_COLUMN_PROPERTY);
                        Matcher matcher = pattern.matcher(columnNames.get(index));
                        if (matcher.find()) {
                            if (listGroupManage.containsKey(matcher.group(1))){
                                //add in Property of this GroupManage
                                Class<?> typeCol = listGroupManage.get(matcher.group(1))
                                        .addElt(columnNames.get(index),index,listColumnTypes.get(index));

                                //Update/Change column Type of all manage column
                                listColumnTypes.set(index,typeCol);
                            } else {
                                logger.debug("searchAllManagedCol()> This column property [{}] doesn't have any NAME_COLUMN_TAGPATH", columnNames.get(index));
                            }

                        }
                    });

            if (listGroupManage.size()>0){
                result = true;
            }else{
                logger.warn("There isn't column with a PK column Name as [{}_YourColumnName.{}]",NAME_COLUMN_PK,NAME_COLUMN_TAG_PATH);
            }

            listGroupManage.entrySet()
                    .stream()
                    .filter(map-> map.getValue().qualityColumnIndex == null &&
                            map.getValue().qualityColumnIndex == null &&
                            map.getValue().qualityColumnIndex == null)
                    .forEach(map ->logger.warn("There isn't column consumer (value/quality/timestamp) of this PK column Name [{}]",map.getValue().columnName));
        }

        return result;

    }

    /**
     * Subscribe OPC item include in dataset
     *
     * @param pyArgs List of all arguments
     * @param keywords List of keywords
     */
    @ScriptFunction(docBundlePrefix = "ClientFct")
    @KeywordArgs(names = {"dataset"},
            types = {Dataset.class})
    public synchronized void subscribe(PyObject[] pyArgs, String[] keywords)
    {
        boolean result = false;
        // If the function is calling without keyword, parameters are tagged with the name indicate in KeywordArgs(names=...)
        try {

            PyArgumentMap args = PyArgumentMap.interpretPyArgs(pyArgs, keywords, ClientFct.class, "subscribe");
            logger.trace("subscribe()> map args:[{}]",args.toString());
            Dataset datasetInput = (Dataset) args.getArg("dataset",null);
            if (datasetInput!=null){
                // unsubscribe previous dataset
                unsubscribeAll();
                if (searchAllManagedCol(datasetInput)){
                    // copy
                    dataset = new BasicDataset(datasetInput.getColumnNames(),listColumnTypes,datasetInput);
                    subscribeDataset(datasetInput);
                }

            } else {
                logger.error("Dataset is null");
            }
        } catch (Exception e) {
            logger.error("error : ",e);
        }
    }

    /**
     * Subscribe all tags found in this Dataset
     *
     * @param ds dataset to manage
     */
    private void subscribeDataset(Dataset ds) {
        // erase lists of subscribed tags
        listTagPath.clear();

        // freezeDataset is copied in a dataset before in case of function call unfreezeAll()
        freezeDataset=null;

        //extract all tag to read in dataset
        if (checkTagClientExist() == false) {
            logger.error("You must create tag client with the name:[{}] to receive the dataset update", TAGPATH_CLIENT_DATASET);
        }

        IntStream.range(0,ds.getRowCount())
                .forEach(rowIndex ->{
                    listGroupManage.forEach( (String columName, GroupColumnsInfo objTagPath) -> {
                        String curTagPath = ds.getValueAt(rowIndex,objTagPath.columnIndex).toString();
                        //Record TagPath and position in DS
                        listTagPath.add(curTagPath);
                        listTagColumName.add(columName);
                        listTagRowIndex.add(rowIndex);
                    });
                        }
                );

       if (listTagPath.isEmpty()){
           logger.debug("subscribeDataset()> No subscriptions to add");
       } else {
           //TODO:Si pb souscription
           boolean result = myGatewayRpc.subscribe("com_api",listTagPath,1000);
           logger.debug("Notify the Gateway to subscribe result:[{}] listTagPath:[{}]",result,listTagPath.toString());

           // create the first time
           if (executionEngine == null) {
               executionEngine = new BasicExecutionEngine();
           }
           startCyclicUpdateTagClientDataset();
           updateTagClientDataset();
       }
    }

    /**
     * Set the TagClientDataset with new update
     */
    private void updateTagClientDataset(){
        try {
            this.clientContext.getTagManager().write(TagPathParser.parse("client", TAGPATH_CLIENT_DATASET), dataset);
            if (dataset==null){
                stopCyclicUpdateTagClientDataset();
            }
        }catch(Exception e){
            logger.error("error : ",e);
        }
    }

    /**
     * Create a periodic Timer to check if a refresh must be done and do it
     */
    private void startCyclicUpdateTagClientDataset(){
        if (executionEngine != null){
            if (executionCyclic == null){
                executionCyclic = new ExecutionCyclic();
            }
            logger.debug("startCyclicUpdateTagClientDataset()> BasicExecutionEngine started rate:[{}]ms",UPDATE_FREQUENCY_MS);
            executionEngine.register("Client-OpcUaFctAddon-Module",EXECUTION_ENGINE_NAME,executionCyclic,UPDATE_FREQUENCY_MS, TimeUnit.MILLISECONDS);
            flagRegistered = true;
        } else {
            logger.error("BasicExecutionEngine is null => create BasicExecutionEngine before starting it");
        }
    }

    /**
     * Stop the periodic Timer to refresh
     */
    private void stopCyclicUpdateTagClientDataset(){
        if (executionEngine != null){
            if (flagRegistered == true) {
                executionEngine.unRegister("Client-OpcUaFctAddon-Module", EXECUTION_ENGINE_NAME);
                logger.debug("stopCyclicUpdateTagClientDataset()> BasicExecutionEngine stopped");
                flagRegistered = false;
            }
        }
    }

    /**
     * Shutdown all cyclic update in progress and close OPC subscription
     */
    private void shutdownCyclicUpdateTagClientDataset(){
        unsubscribeAll();
        if (executionEngine != null){
            executionEngine.shutdown();
            logger.debug("shutdownCyclicUpdateTagClientDataset()> BasicExecutionEngine shutdown");
        }
    }

    /**
     * Shutdown an release
     */
    public void shutdown(){
        stopCyclicUpdateTagClientDataset();
        shutdownCyclicUpdateTagClientDataset();
        myGatewayRpc.notifyShutdown();
        logger.debug("ClientFct shutdown");
    }

    /**
     * Unsubscribe all current OPC tags in progress
     */
    @ScriptFunction(docBundlePrefix = "ClientFct")
    public synchronized void unsubscribeAll() {
        try {
            if ((listTagPath != null)){
                if (!listTagPath.isEmpty() ){
                    myGatewayRpc.unSubscribe();
                    logger.debug("unsubscribeAll()> Delete subscriptions for this client in Gateway of [{}] tags",listTagPath.size());
                    listTagPath.clear();
                    listTagRowIndex.clear();
                    listTagColumName.clear();
                }
            }
            dataset = null;
            updateTagClientDataset();
            freezeDataset = null;
        } catch (Exception e) {
            logger.error("error : ",e);
        }
    }

    /**
     * Freeze the state of the current subscription
     */
    @ScriptFunction(docBundlePrefix = "ClientFct")
    public synchronized void freezeAll() {
        try {
            logger.debug("freezeAll()");
            if (listTagPath != null){
                if (!listTagPath.isEmpty() ){
                    myGatewayRpc.unSubscribe();
                    logger.debug("freezeAll()> Delete the current subscription for this client in Gateway of [{}] tags",listTagPath.size());
                    listTagPath.clear();
                    listTagRowIndex.clear();
                    listTagColumName.clear();
                }
            }

            //memorize before erase
            if (dataset != null){
                freezeDataset = new BasicDataset(dataset);
                logger.debug("freezeAll()> Save of [{}] rows",freezeDataset.getRowCount());
                freezeListGroupManage.putAll(listGroupManage);

                // dataset erase
                dataset = null;

                updateTagClientDataset();
            } else {
                freezeDataset = null;
                logger.debug("freezeAll()> Save of 0 rows");
                freezeListGroupManage.clear();
            }
        } catch (Exception e) {
            logger.error("error : ",e);
        }
    }

    /**
     * Unfreeze the current Tags refresh in dataset
     */
    @ScriptFunction(docBundlePrefix = "ClientFct")
    public synchronized void unfreezeAll() {
        try {
            logger.debug("unfreezeAll()");
            //initialize with memorized data before erase
            if (freezeDataset != null){
                dataset = new BasicDataset(freezeDataset);
                listGroupManage.putAll(freezeListGroupManage);
                subscribeDataset(dataset);
            }
        } catch (Exception e) {
            logger.error("error : ",e);
        }
    }


    /**
     * Check if the dataset tag client exist in scope client
     * @return boolean True if exist
     */
    private boolean checkTagClientExist(){
        List<TagPath> listRead = new ArrayList<TagPath>();
        try {
            listRead.add(TagPathParser.parse("client", TAGPATH_CLIENT_DATASET));
            List<QualifiedValue> listQv = this.clientContext.getTagManager().read(listRead);
            if (listQv.isEmpty()==false){
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            logger.error("error : ",e);
            return false;
        }
    }


    /**
     * Internal Class to receive notification from the local gateway
     */
   private class MyGatewayNotificationListener extends FilteredPushNotificationListener {

        public MyGatewayNotificationListener(String moduleId, String... messageTypes) {
            super(moduleId, messageTypes);
        }

        /**
         * Reception of notification
         * @param pushNotification Msg notification with all new Value
         */
        @Override
        protected void receive(PushNotification pushNotification) {

                //Force new refresh
                List<QualifiedValue> lstNewValue = (List<QualifiedValue>) pushNotification.getMessage();

                if (lstNewValue == null) {
                    logger.error("MyGatewayNotificationListener.receive()> Null Message");
                } else {
                    if (lstNewValue.size() == listTagPath.size()) {
                        updateCellDataset(lstNewValue);
                    } else {
                        logger.error("MyGatewayNotificationListener.receive()> wrong Size of List<QualifiedValue>");
                    }
                }
                logger.debug("MyGatewayNotificationListener.receive()> Receive a notification lstNewValue:[{}]", lstNewValue);

        }

    }

    /**
     * Update cells in dataset with a list of new values
     * @param listValue List of all QualifiedValue (with new value)
     */
    private void updateCellDataset(List<QualifiedValue>  listValue){
        IntStream.range(0, listValue.size())
                .forEach(index -> {
                    QualifiedValue value = listValue.get(index);
                    String columnName = listTagColumName.get(index);
                    int rowIndex = listTagRowIndex.get(index);
                    listGroupManage.get(columnName).setValueAtDataset(value, rowIndex);
                });
        //A update cell was perform
        updateTagClientFlag.set(true);

    }

    /**
     * Apply the update receive notification to the Tag client only if there are changes
     */
    private class ExecutionCyclic implements Runnable{
        @Override
        public void run() {

            //logger.trace("Execute CyclicUpdateTagClientDataset");
            // updateTagClient.compareAndSet(expect, update)
            // expect => expected value
            // update => update value if the value is equal to expected
            // return : true => if value updated
            // to avoid that this flag is updated elsewhere
            if (updateTagClientFlag.compareAndSet(true,false)){
                updateTagClientDataset();
            }
        }

    }







}
