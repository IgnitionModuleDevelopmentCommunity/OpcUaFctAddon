package com.bouyguesenergiesservices.opcuafctaddon.gateway.fct;


import com.bouyguesenergiesservices.opcuafctaddon.gateway.opc.SubscribableNodeCallback;
import com.inductiveautomation.ignition.common.execution.ExecutionManager;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.opc.BasicServerNodeId;
import com.inductiveautomation.ignition.common.opc.ServerNodeId;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.opc.OPCManager;
import com.inductiveautomation.ignition.gateway.opc.SubscribableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by regis on 19/10/2016.
 *
 * Implementation of GatewayFct OPC AddOn in Gateway
 */
public abstract class GatewayFct{

    public static final int RATE_TIMEOUT_MANAGER = 10000; //ms

    public final Logger logger = LoggerFactory.getLogger(getClass());

    public final OPCManager opcm;
    public final ExecutionManager execm;
    public final GatewayContext context;

    //Client Communication
    private final String sessionId;
    private NotifyManager notifyManager = new NotifyManager();
    private boolean isNotifyManagerStarted = false;

    //Subscription OPC Elements
    private List<SubscribableNode> lstNode;
    private int rate;
    private AtomicBoolean valueChanged = new AtomicBoolean(false);

    public GatewayFct(GatewayContext _context,String _sessionId){
        this.opcm = _context.getOPCManager();
        this.execm = _context.getExecutionManager();
        this.context = _context;
        this.sessionId = _sessionId;
    }


    /**
     * Declare an OPC Subscription (locally ) with lstItemPath (only integer type).
     *
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstOPCItemPath A list of strings, each representing an item path, or address to read from.
     * @param rate Frequency of the subscription
     *
     * @return boolean True if the subscription is right declare
     */
    public boolean subscribe(String opcServer, List<String> lstOPCItemPath, int rate) {
        boolean result = false;

        //unsubscribe previous
        opcm.cancelSubscription(sessionId);

        //create a new empty OPC subscription
        opcm.setSubscriptionRate(sessionId,rate);

        //prepare structure 'SubscribableNodeCallback'
        if (prepareSubscription(lstOPCItemPath,opcServer,sessionId,rate)){

            //Make first read to initialize Value quickly
            initializeValues(opcServer, lstOPCItemPath);

            //Create OPC subscription
            opcm.subscribe(lstNode);
            logger.debug("subscribe() > declare new subscription sessionId:[{}]", sessionId);
            result = true;
        } else {
            //cancel OPC subscription
            opcm.cancelSubscription(sessionId);
            logger.debug("subscribe() > cancel to declare new subscription sessionId:[{}]", sessionId);
        }
        return result;
    }


    /**
     * Unsubscribe OPC Item
     */
    public void unSubscribe() {
        logger.trace("unSubscribe()> listNode [{}] sessionId:[{}]",lstNode, sessionId);
        if (lstNode!=null) {
            if (lstNode.isEmpty()) {
                logger.debug("unsubscribe()> There is no OPC Node to unsubscribe sessionId:[{}]",sessionId);
            } else {
                //unsubscribe in OPC
                opcm.unsubscribe(lstNode);
                lstNode.clear();
                logger.debug("unsubscribe()> All OPC Node are unsubscribe lstNode:[{}] sessionId:[{}]", lstNode.toString(),sessionId);
            }

        }
    }

    public void shutdown(){
        logger.trace("shutdown()> Received shutdown request sessionId:[{}]",sessionId);
        if (!lstNode.isEmpty()){
            unSubscribe();
        }
        //stop timer to notifyManager / timeoutManager
        execm.unRegister(sessionId,NotifyManager.class.getName());
        isNotifyManagerStarted = false;

    }

    /**
     * Create all SubscribableNodeCallback structure for callback notification from OPC API
     *
     * @param lstOPCItemPath List of OPC Node name
     * @param opcServer OPC Server declare in Gateway
     * @param subscriptionName Name of this subscription
     * @param rate Frequency update
     * @return True if everything is prepare
     */
    private boolean prepareSubscription(List<String> lstOPCItemPath, String opcServer, String subscriptionName, int rate){
        boolean result=true;

        //TODO: Gere autre type de variable
        //Create all SubscribableNodeCallback for each tag subscribe
       this.lstNode = new ArrayList<>(lstOPCItemPath
                .stream()
                .map(itemPath -> new SubscribableNodeCallback(new BasicServerNodeId(opcServer,itemPath),subscriptionName, DataType.Int4,valueChanged))
                .collect(Collectors.toList()));
        logger.debug("prepareSubscription()> listNode:[{}] sessionId:[{}]",lstNode,sessionId);


        this.rate = rate;
        if (!isNotifyManagerStarted){
            //Register the cyclic to manage notify changed Tag client
            logger.debug("prepareSubscription()> Declare a cyclic execution notifyManager rate:[{}ms] sessionId:[{}]",rate, sessionId);
            execm.register(sessionId,NotifyManager.class.getName(), notifyManager,rate, TimeUnit.MILLISECONDS);
            isNotifyManagerStarted = true;
        } else {
            logger.warn("prepareSubscription()> NotifyManager is already started previous rate:[{}]",this.rate);
        }

        return result;
    }

    /**
     * Simple read value to the list OPC Item before subscription was initiate
     *
     * @param opcServer OPC Server declare in Gateway
     * @param lstItemPath List of all Node name
     */
    private void initializeValues(String opcServer, List<String> lstItemPath){
        //Convert List<String> to List<ServerNodeId>
        List<ServerNodeId> lstServerNode =  lstItemPath
                .stream()
                .map(itemPath -> new BasicServerNodeId(opcServer,itemPath))
                .collect(Collectors.toList());

        //start an OPC reading
        List<QualifiedValue> qualifiedValues = opcm.read(lstServerNode);

        notifyMyConsumer(qualifiedValues);

    }

    @Override
    public String toString(){
        return String.format("sessionId:[%s] lstNode:[%s]",sessionId,lstNode);
    }


    /**
     * Class to managed (Generate) client notification
     */
    private class NotifyManager implements Runnable {

        @Override
        public void run() {
            logger.trace("NotifyManager.run()> sessionId:[{}]",sessionId);
            if (!lstNode.isEmpty()) {
                if (valueChanged.compareAndSet(true, false)) {
                    //Notify client
                    try {
                        logger.debug("NotifyManager.run()> Sending notification to the client sessionId:[{}]", sessionId);

                        List<QualifiedValue> listNewValue = lstNode.stream()
                                .map(SubscribableNode::getLastSubscriptionValue)
                                .collect(Collectors.toList());

                        //it could be Ignition Client Notification or Gateway (GAN) Notification
                        notifyMyConsumer(listNewValue);
                    } catch (Exception ex) {
                        logger.error("NotifyManager.run()>Erreur sending notification to the consumer sessionId:[{}]",sessionId, ex);
                    }
                }
            }
        }
    }

    /**
     * The consumer of GatewayFct Notification value change
     *
     * @param listNewValue List of all new Value
     */
    public abstract void notifyMyConsumer(List<QualifiedValue> listNewValue);

}
