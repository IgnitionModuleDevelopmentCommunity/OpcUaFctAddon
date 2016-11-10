package com.bouyguesenergiesservices.gateway.service;

import com.bouyguesenergiesservices.gateway.GetOPCGatewayFunctions;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Call 'GetOPCGatewayFunctions' to apply demand from the service
 * Created by regis on 04/11/2016.
 */
public class GetOPCServiceImpl implements GetOPCService{

    private GatewayContext context;
    private GetOPCGatewayFunctions scriptModule;
    private final Logger logger = LoggerFactory.getLogger(getClass());


    public GetOPCServiceImpl(GatewayContext context, GetOPCGatewayFunctions scriptModule){
        this.context = context;
        this.scriptModule = scriptModule;
    }

    @Override
    public List<QualifiedValue> getServiceReadValues(String opcServer, List<String> lstItemPath) {

        logger.debug("getServiceReadValues()> Scope local gateway");

        return scriptModule.readValues(opcServer,lstItemPath);

    }

    @Override
    public boolean getServiceIsSubscribe(String subscriptionName) {
        logger.debug("getServiceIsSubscribe()> Scope local gateway");
        return scriptModule.isSubscribe(subscriptionName);
    }

    @Override
    public String getServiceSubscribe(String opcServer, List<String> lstItemPath, int rate) {
        logger.debug("getServiceSubscribe()> Scope local gateway");
        return scriptModule.subscribe(opcServer, lstItemPath, rate);
    }

    @Override
    public List<QualifiedValue> getServiceReadSubscribeValues(String subscriptionName) {
        logger.debug("getServiceReadSubscribeValues()> Scope local gateway");
        return scriptModule.readSubscribeValues(subscriptionName);
    }

    @Override
    public boolean getServiceUnsubscribe(String subscriptionName) {
        logger.debug("getServiceUnsubscribe()> Scope local gateway");
        return scriptModule.unsubscribe(subscriptionName);
    }

    @Override
    public void getServiceUnsubscribeAll() {
        logger.debug("getServiceUnsubscribeAll()> Scope local gateway");
        scriptModule.unsubscribeAll();
    }
}
