package com.bouyguesenergiesservices.client;

import com.bouyguesenergiesservices.GetOPCRPC;
import com.bouyguesenergiesservices.GetOPCScriptFunctions;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Created by regis on 18/10/2016.
 */
public class GetOPCClientFunctions extends GetOPCScriptFunctions {

    private final GetOPCRPC script;

    public GetOPCClientFunctions(){
        script = ModuleRPCFactory.create("com.bouyguesenergiesservices.ignition.OpcUaFctAddon", GetOPCRPC.class);
    }

    @Override
    protected List<QualifiedValue> readValuesImpl(String opcServer, List<String> lstItemPath) {
        return script.readValues(opcServer,lstItemPath);
    }


    @Override
    protected boolean isSubscribeImpl(String subscriptionName) {
        return script.isSubscribe(subscriptionName);
    }

    @Override
    protected String subscribeImpl(String opcServer, List<String> lstItemPath, int rate) {
        return script.subscribe(opcServer, lstItemPath,rate);
    }

    @Override
    protected List<QualifiedValue> readSubscribeValuesImpl(String subscriptionName) {
        return  script.readSubscribeValues(subscriptionName);
    }

    @Override
    protected boolean unsubscribeImpl(String subscriptionName) {
        return script.unsubscribe(subscriptionName);
    }

    @Override
    protected void unsubscribeAllImpl() {
        script.unsubscribeAll();
    }


    //GAN Implementation Functions

    @Override
    protected List<QualifiedValue> getRemoteReadValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath) {
        return script.getRemoteReadValues(remoteServer, opcServer, lstItemPath);
    }

    @Override
    protected boolean getRemoteIsSubscribeImpl(String remoteServer, String subscriptionName) {
        return script.getRemoteIsSubscribe(remoteServer, subscriptionName);
    }

    @Override
    protected String getRemoteSubscribeImpl(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {
        return script.getRemoteSubscribe( remoteServer, opcServer, lstItemPath, rate);
    }

    @Override
    protected List<QualifiedValue> getRemoteReadSubscribeValuesImpl(String remoteServer, String subscriptionName) {
        return script.getRemoteReadSubscribeValues(remoteServer, subscriptionName);
    }

    @Override
    protected boolean getRemoteUnsubscribeImpl(String remoteServer, String subscriptionName) {
        return script.getRemoteUnsubscribe(remoteServer, subscriptionName);
    }

    @Override
    protected void getRemoteUnsubscribeAllImpl(String remoteServer) {
        script.getRemoteUnsubscribeAll(remoteServer);
    }


}
