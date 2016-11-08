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

        logger.info("Scope local gateway : getServiceReadValues");

        return scriptModule.readValues(opcServer,lstItemPath);

    }

    @Override
    public boolean getServiceIsSubscribe(String subscriptionName) {
        logger.info("Scope local gateway : getServiceIsSubscribe");
        return scriptModule.isSubscribe(subscriptionName);
    }

    @Override
    public String getServiceSubscribe(String opcServer, List<String> lstItemPath, int rate) {
        logger.info("Scope local gateway : getServiceSubscribe");
        return scriptModule.subscribe(opcServer, lstItemPath, rate);
    }

    @Override
    public List<QualifiedValue> getServiceReadSubscribeValues(String subscriptionName) {
        logger.info("Scope local gateway : getServiceReadSubscribeValues");
        return scriptModule.readSubscribeValues(subscriptionName);
    }

    @Override
    public boolean getServiceUnsubscribe(String subscriptionName) {
        logger.info("Scope local gateway : getServiceUnsubscribe");
        return scriptModule.unsubscribe(subscriptionName);
    }

    @Override
    public void getServiceUnsubscribeAll() {
        logger.info("Scope local gateway : getServiceUnsubscribeAll");
        scriptModule.unsubscribeAll();
    }
}
