package com.bouyguesenergiesservices.gateway;


import com.bouyguesenergiesservices.gateway.opc.SubscribableNodeCallback;
import com.bouyguesenergiesservices.GetOPCScriptFunctions;
import com.inductiveautomation.ignition.common.execution.ExecutionManager;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.opc.BasicServerNodeId;
import com.inductiveautomation.ignition.common.opc.ServerNodeId;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.gateway.opc.OPCManager;
import com.inductiveautomation.ignition.gateway.opc.SubscribableNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


/**
 * Created by regis on 19/10/2016.
 */
public class GetOPCGatewayFunctions extends GetOPCScriptFunctions {
    private static final int RATE_TIMEOUT_MANAGER = 10000;
    private static final Long TIMEOUT_SUBS = 30000L; //ms

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final OPCManager opcm;
    private final ExecutionManager execm;

    private GetOPCRPCImpl rpc;

    //private AtomicReference<Map<String,SubscriptionConfig>> atomLstSubscription = new AtomicReference<>();
    private ConcurrentHashMap<String,SubscriptionConfig> atomLstSubscription = new ConcurrentHashMap<>();


    public GetOPCGatewayFunctions(OPCManager opcm, ExecutionManager execm,GetOPCRPCImpl rpc){
        this.opcm = opcm;
        this.execm = execm;
        this.rpc = rpc;

        //Creation du gestionnaire de timeout des souscriptions
        TimeoutSubscriptionManager timeoutManager = new TimeoutSubscriptionManager();
        execm.register("ByesFctExpose","GetOPCGatewayFunctions", timeoutManager,RATE_TIMEOUT_MANAGER, TimeUnit.MILLISECONDS);

    }

    private void addRefSubscription(String subscriptionName,SubscriptionConfig configSubs){
        atomLstSubscription.putIfAbsent(subscriptionName,configSubs);

    }

    private SubscriptionConfig getRefSubscriptionConfig(String subscriptionName){
        return atomLstSubscription.get(subscriptionName);
    }

    private  Map<String,SubscriptionConfig> getAllSubscriptionConfig(){
        return atomLstSubscription;
    }

    private Set<String> getKeysSubscriptionConfig(){
        return atomLstSubscription.keySet();
    }

    private void updateRefSubscriptionConfig(String name, SubscriptionConfig subsConfig){
        atomLstSubscription.replace(name,subsConfig);
    }

    private void removeRefSubscriptionConfig(String name){

        atomLstSubscription.remove(name);

    }


    /***
     * Lecture OPC Basic
     ***/
    @Override
    protected List<QualifiedValue> readValuesImpl(String opcServer, List<String> lstItemPath) {

        //Conversion de la List<String> en List<ServerNodeId>
        List<ServerNodeId> lstNode =  lstItemPath
                .stream()
                .map(itemPath -> new BasicServerNodeId(opcServer,itemPath))
                .collect(Collectors.toList());


        //Lancement de la lecture
        return  opcm.read(lstNode);
    }


    @Override
    protected String subscribeImpl(String opcServer, List<String> lstItemPath, int rate) {

        String subscriptionName = String.format("Subscribe_%s" ,UUID.randomUUID());
        opcm.setSubscriptionRate(subscriptionName,rate);
        logger.debug("subscribeImpl - Creation de la souscription:{}",subscriptionName);

        SubscriptionConfig configSubs = new SubscriptionConfig(subscriptionName,opcServer, lstItemPath);
        logger.debug("subscribeImpl - Creation du referentiel {}",configSubs);
        opcm.subscribe(configSubs.getListSubscribableNode());


        //Enregistrement de la liste Tag de la souscription
        addRefSubscription(subscriptionName,configSubs);

        return subscriptionName;
    }


    @Override
    protected boolean isSubscribeImpl(String subscriptionName) {

        SubscriptionConfig subsConfig = getRefSubscriptionConfig(subscriptionName);

        return subsConfig != null;

    }


    @Override
    public List<QualifiedValue> readSubscribeValuesImpl(String subscriptionName) {

        SubscriptionConfig subsConfig = getRefSubscriptionConfig(subscriptionName);

        if (subsConfig!=null){

            if (subsConfig.valueChanged.get()) {
                logger.info("{}",subscriptionName);
                //List<QualifiedValue>
                List<QualifiedValue> result = subsConfig.getListSubscribableNode()
                        .stream().map(SubscribableNode::getLastSubscriptionValue)
                        .collect(Collectors.toList());

                logger.info("Resultat renvoye {} {}",subscriptionName,result.toString());

                //remise a 0 Changement Valeur + Lecture Client
                subsConfig.lastClientRead.set(System.currentTimeMillis());
                subsConfig.valueChanged.set(false);
                updateRefSubscriptionConfig(subscriptionName, subsConfig);

                return result;
            } else {
                //Lecture Client
                subsConfig.lastClientRead.set(System.currentTimeMillis());
                logger.debug("readSubscribeValuesImpl - Pas de valeurs changees");
            }


        }return null;
    }


    @Override
    protected boolean unsubscribeImpl(String subscriptionName) {

        SubscriptionConfig subsConfig = getRefSubscriptionConfig(subscriptionName);

        if (subsConfig!=null){

            //desenregistrement de la souscription
            opcm.unsubscribe(subsConfig.getListSubscribableNode());

            //liberation des ressources de cette souscription
            removeRefSubscriptionConfig(subscriptionName);

            logger.debug("unsubscribeImpl - Desabonnement [{}]",subscriptionName);
            return true;
        }

        return false;
    }




    @Override
    public void unsubscribeAllImpl() {
        //Liberation de chaque souscription
        getKeysSubscriptionConfig().forEach(this::unsubscribeImpl);

    }

    @Override
    protected List<QualifiedValue> getRemoteReadValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath) {
        return rpc.getRemoteReadValues(remoteServer,opcServer,lstItemPath);
    }

    @Override
    public boolean getRemoteIsSubscribeImpl(String remoteServer, String subscriptionName) {
        return rpc.getRemoteIsSubscribe(remoteServer,subscriptionName);
    }

    @Override
    protected String getRemoteSubscribeImpl(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {
        return rpc.getRemoteSubscribe(remoteServer, opcServer, lstItemPath, rate);
    }

    @Override
    protected List<QualifiedValue> getRemoteReadSubscribeValuesImpl(String remoteServer, String subscriptionName) {
        return rpc.getRemoteReadSubscribeValues(remoteServer, subscriptionName);
    }

    @Override
    protected boolean getRemoteUnsubscribeImpl(String remoteServer, String subscriptionName) {
        return rpc.getRemoteUnsubscribe(remoteServer, subscriptionName);
    }

    @Override
    protected void getRemoteUnsubscribeAllImpl(String remoteServer) {
        rpc.getRemoteUnsubscribeAll(remoteServer);
    }


    public void shutdownGatewayScriptModule(){
        unsubscribeAllImpl();
        execm.unRegisterAll("ByesFctExpose");

    }



    /**
     * Descripteur de la souscription
     */
    private class SubscriptionConfig {
        private final String name;
        private AtomicBoolean valueChanged = new AtomicBoolean(false);
        private final List<SubscribableNode> lstNode;
        private AtomicLong lastClientRead = new AtomicLong(0L);


        SubscriptionConfig(String name, String opcServer, List<String> lstItemPath){
            this.name = name;
            this.lastClientRead.set(System.currentTimeMillis());

            //Conversion de la List<String> en List<SubscribableNode>
            this.lstNode = lstItemPath
                    .stream()
                    .map(itemPath -> new SubscribableNodeCallback(new BasicServerNodeId(opcServer,itemPath),name, DataType.Int4,valueChanged))
                    .collect(Collectors.toList());



        }


        List<SubscribableNode> getListSubscribableNode(){
            return lstNode;
        }

        String getName(){
            return name;
        }

        public String toString(){
            return String.format("SubscriptionConfig - name:[%s], lstNode:[%s], lastClientRead:[%s], valueChanged:[%s]",name,lstNode,lastClientRead,valueChanged);
        }

    }


    private class TimeoutSubscriptionManager implements Runnable {

        @Override
        public void run(){

            Long currentTimeMillis = System.currentTimeMillis();
            List<String> lstNameSuppress = new ArrayList<>();

            logger.trace("TimeoutSubscriptionManager.run() currentTime:[{}]",currentTimeMillis);


            getAllSubscriptionConfig().values().stream()
                    //filtre sur les souscriptions en Timeout
                    .filter(subConfig -> (currentTimeMillis -  subConfig.lastClientRead.get()) > TIMEOUT_SUBS)
                    //Libere les ressources
                    .forEach((subConfig) ->{
                        opcm.unsubscribe(subConfig.getListSubscribableNode());
                        lstNameSuppress.add(subConfig.getName());
                        logger.debug("TimeoutSubscriptionManager.run() - Timeout Cloture[{}]",subConfig.getName());
                    });


            //Suppression de la liste des souscriptions
            lstNameSuppress.forEach(GetOPCGatewayFunctions.this::removeRefSubscriptionConfig);
            lstNameSuppress.clear();

        }
    }

}
