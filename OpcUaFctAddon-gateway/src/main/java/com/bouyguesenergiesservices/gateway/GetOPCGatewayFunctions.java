package com.bouyguesenergiesservices.gateway;


import com.bouyguesenergiesservices.gateway.opc.SubscribableNodeCallback;
import com.bouyguesenergiesservices.GetOPCScriptFunctions;
import com.bouyguesenergiesservices.gateway.service.GetOPCService;
import com.inductiveautomation.ignition.common.execution.ExecutionManager;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.opc.BasicServerNodeId;
import com.inductiveautomation.ignition.common.opc.ServerNodeId;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.gateway.gan.GatewayAreaNetworkManager;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.opc.OPCManager;
import com.inductiveautomation.ignition.gateway.opc.SubscribableNode;

import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.api.ServiceManager;
import com.inductiveautomation.metro.api.services.ServiceState;
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
    private final GatewayAreaNetworkManager ganm;
    private final String serverCurrentName;

    //Creation du gestionnaire de timeout des souscriptions
    private TimeoutSubscriptionManager timeoutManager = new TimeoutSubscriptionManager();

    private ConcurrentHashMap<String,SubscriptionConfig> atomLstSubscription = new ConcurrentHashMap<>();

    public GetOPCGatewayFunctions(GatewayContext context){
        this.opcm = context.getOPCManager();
        this.execm = context.getExecutionManager();
        this.ganm = context.getGatewayAreaNetworkManager();
        this.serverCurrentName = ganm.getServerAddress().getServerName();

    }

    private void addRefSubscription(String subscriptionName,SubscriptionConfig configSubs){

        //Relance du gestion de Timeout de souscription si 1 elt dans la liste des souscriptions
        if (atomLstSubscription.isEmpty()){
            logger.debug("addRefSubscription()> Relance du gestionnaire de souscription");
            execm.register("OpcUaFctAddon",TimeoutSubscriptionManager.class.getName(), timeoutManager,RATE_TIMEOUT_MANAGER, TimeUnit.MILLISECONDS);
        }

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

        if (atomLstSubscription.isEmpty()){
            logger.debug("removeRefSubscriptionConfig()> Arret du gestionnaire de souscription");
            execm.unRegister("OpcUaFctAddon",TimeoutSubscriptionManager.class.getName());
        }

    }


    @Override
    protected List<QualifiedValue> readValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath) {


        if (remoteServer.equals(serverCurrentName)){
            logger.debug("readValuesImpl()> Fct traite en locale");
            return readValues(opcServer, lstItemPath);
        } else {

            GetOPCService opcService = callGANService(remoteServer);

            if (opcService == null) {
                logger.debug("readValuesImpl()> Service via GAN indisponible");
                return null;
            }
            else {
                logger.debug("readValuesImpl()> Service via GAN accessible");
                return opcService.getServiceReadValues(opcServer, lstItemPath);
            }

        }

    }


    @Override
    protected String subscribeImpl(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {

        if (remoteServer.equals(serverCurrentName)){
            logger.debug("subscribeImpl()> Fct traite en locale");
            return subscribe(opcServer, lstItemPath, rate);
        } else {

            GetOPCService opcService = callGANService(remoteServer);

            if (opcService == null) {
                logger.debug("subscribeImpl()> Service via GAN indisponible");
                return null;
            }
            else {
                logger.debug("subscribeImpl()> Service via GAN accessible");
                return opcService.getServiceSubscribe(opcServer, lstItemPath, rate);
            }

        }
    }


    @Override
    protected boolean isSubscribeImpl(String remoteServer,String subscriptionName) {
        if (remoteServer.equals(serverCurrentName)){
            if (logger.isDebugEnabled()) logger.debug("isSubscribeImpl()> Fct traite en locale");
            return isSubscribe(subscriptionName);
        } else {

            GetOPCService opcService = callGANService(remoteServer);

            if (opcService == null) {
                logger.debug("isSubscribeImpl()> Service via GAN indisponible");
                return false;
            }
            else {
                logger.debug("isSubscribeImpl()> Service via GAN accessible");
                return opcService.getServiceIsSubscribe(subscriptionName);
            }
        }
    }


    @Override
    protected List<QualifiedValue> readSubscribeValuesImpl(String remoteServer,String subscriptionName) {

        if (remoteServer.equals(serverCurrentName)){
            logger.debug("readSubscribeValuesImpl()> Fct traite en locale");
            return readSubscribeValues(subscriptionName);
        } else {

            GetOPCService opcService = callGANService(remoteServer);

            if (opcService == null) {
                logger.debug("readSubscribeValuesImpl()> Service via GAN indisponible");
                return null;
            }
            else {
                logger.debug("readSubscribeValuesImpl()> Service via GAN accessible");
                return opcService.getServiceReadSubscribeValues(subscriptionName);
            }
        }

    }


    @Override
    protected boolean unsubscribeImpl(String remoteServer, String subscriptionName) {

        logger.trace("unsubscribeAllImpl()> [remoteServerId:{}, localServerId:{}]",remoteServer,serverCurrentName);
        if (remoteServer.equals(serverCurrentName)){
            logger.debug("unsubscribeImpl()> Fct traite en locale");
            return unsubscribe(subscriptionName);
        } else {

            GetOPCService opcService = callGANService(remoteServer);

            if (opcService == null) {
                logger.debug("unsubscribeImpl()> Service via GAN indisponible");
                return false;
            }
            else {
                logger.debug("unsubscribeImpl()> Service via GAN accessible");
                return opcService.getServiceUnsubscribe(subscriptionName);
            }
        }
    }




    @Override
    protected void unsubscribeAllImpl(String remoteServer) {

      logger.trace("unsubscribeAllImpl()> [remoteServerId:{}, localServerId:{}]",remoteServer,serverCurrentName);
        if (remoteServer.equals(serverCurrentName)){
            logger.debug("unsubscribeAllImpl()> Fct traite en locale");
            unsubscribeAll();
        } else {

            GetOPCService opcService = callGANService(remoteServer);

            if (opcService == null) {
                logger.debug("unsubscribeAllImpl()> Service via GAN indisponible");
            }
            else {
                logger.debug("unsubscribeAllImpl()> Service via GAN accessible");
                opcService.getServiceUnsubscribeAll();
            }
        }

    }


    /***
     * Lecture OPC Basic Local
     ***/
    public List<QualifiedValue> readValues(String opcServer, List<String> lstItemPath) {

        //Conversion de la List<String> en List<ServerNodeId>
        List<ServerNodeId> lstNode =  lstItemPath
                .stream()
                .map(itemPath -> new BasicServerNodeId(opcServer,itemPath))
                .collect(Collectors.toList());


        //Lancement de la lecture
        return  opcm.read(lstNode);
    }



    public String subscribe(String opcServer, List<String> lstItemPath, int rate) {

        String subscriptionName = String.format("Subscribe_%s" ,UUID.randomUUID());
        opcm.setSubscriptionRate(subscriptionName,rate);
        logger.debug("subscribeImpl()> Creation de la souscription [subscriptionName:{}]",subscriptionName);


        SubscriptionConfig configSubs = new SubscriptionConfig(subscriptionName,opcServer, lstItemPath);
        logger.debug("subscribeImpl()> Creation du referentiel de la souscription[{}]",configSubs);
        opcm.subscribe(configSubs.getListSubscribableNode());



        //Enregistrement de la liste Tag de la souscription
        addRefSubscription(subscriptionName,configSubs);

        return subscriptionName;
    }

    public boolean isSubscribe(String subscriptionName) {

        SubscriptionConfig subsConfig = getRefSubscriptionConfig(subscriptionName);

        return subsConfig != null;

    }



    public List<QualifiedValue> readSubscribeValues(String subscriptionName) {

        SubscriptionConfig subsConfig = getRefSubscriptionConfig(subscriptionName);

        if (subsConfig!=null){

            if (subsConfig.valueChanged.get()) {
                //List<QualifiedValue>
                List<QualifiedValue> result = subsConfig.getListSubscribableNode()
                        .stream().map(SubscribableNode::getLastSubscriptionValue)
                        .collect(Collectors.toList());

                if (logger.isDebugEnabled()) logger.debug("readSubscribeValues()> Resultats renvoye [subscriptionName:{}, values:'{}']",subscriptionName,result.toString());

                //remise a 0 Changement Valeur + Lecture Client
                subsConfig.lastClientRead.set(System.currentTimeMillis());
                subsConfig.valueChanged.set(false);
                updateRefSubscriptionConfig(subscriptionName, subsConfig);

                return result;
            } else {
                //Lecture Client
                subsConfig.lastClientRead.set(System.currentTimeMillis());
                if (logger.isDebugEnabled()) logger.debug("readSubscribeValues()> Pas de valeurs changees");
            }


        }return null;
    }



    public boolean unsubscribe(String subscriptionName) {

        SubscriptionConfig subsConfig = getRefSubscriptionConfig(subscriptionName);

        if (subsConfig!=null){

            //desenregistrement de la souscription
            opcm.unsubscribe(subsConfig.getListSubscribableNode());

            //liberation des ressources de cette souscription
            removeRefSubscriptionConfig(subscriptionName);

            logger.debug("unsubscribe()> Desabonnement [subscriptionName{}]",subscriptionName);
            return true;
        }

        return false;
    }





    public void unsubscribeAll() {
        //Liberation de chaque souscription
        getKeysSubscriptionConfig().forEach(this::unsubscribe);

    }




    public void shutdownGatewayScriptModule(){
        //liberation des ressources allouees
        unsubscribeAll();
        execm.unRegisterAll("OpcUaFctAddon");
    }


    /**
     * Function 'GetOPCService' Interface available on any Gateway
     * @param remoteServer Name of the remote Gateway Server
     * @return return an interface GetOPCService available on the remoteServer
     */
    private GetOPCService callGANService(String remoteServer){
        GetOPCService opcService = null;
        ServiceManager sm = ganm.getServiceManager();
        ServerId serverId = ServerId.fromString(remoteServer);

        // First, verify that the service is available on the remote machine before trying to call.
        ServiceState state = sm.getRemoteServiceState(serverId, GetOPCService.class);
        if (state != ServiceState.Available){
            logger.warn("callGANService()> Service indisponible [serveur:{}, currentState:{}]",
                    serverId.toDescriptiveString(),
                    state.toString());
        } else {
            // The service call will time out after 60 seconds if no response is received from the remote Gateway.
            opcService =  sm.getService(serverId,GetOPCService.class).get();
        }
        return opcService;
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
            return String.format("SubscriptionConfig.getListSubscribableNode() > name:%s, lstNode:%s, lastClientRead:%s, valueChanged:%s",name,lstNode,lastClientRead,valueChanged);
        }

    }


    private class TimeoutSubscriptionManager implements Runnable {

        @Override
        public void run(){

            Long currentTimeMillis = System.currentTimeMillis();
            List<String> lstNameSuppress = new ArrayList<>();

            logger.trace("TimeoutSubscriptionManager.run()> Gestionnaire de Timout [currentTime:{}ms]",currentTimeMillis);


            getAllSubscriptionConfig().values().stream()
                    //filtre sur les souscriptions en Timeout
                    .filter(subConfig -> (currentTimeMillis -  subConfig.lastClientRead.get()) > TIMEOUT_SUBS)
                    //Libere les ressources
                    .forEach((subConfig) ->{
                        opcm.unsubscribe(subConfig.getListSubscribableNode());
                        lstNameSuppress.add(subConfig.getName());
                       logger.debug("TimeoutSubscriptionManager.run()> Timeout Cloture [subcriptionName:{}]",subConfig.getName());
                    });


            //Suppression de la liste des souscriptions
            lstNameSuppress.forEach(GetOPCGatewayFunctions.this::removeRefSubscriptionConfig);
            lstNameSuppress.clear();

        }
    }

}
