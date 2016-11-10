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
    protected List<QualifiedValue> readValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath) {
        return script.readValues(remoteServer,opcServer,lstItemPath);
    }


    @Override
    protected boolean isSubscribeImpl(String remoteServer, String subscriptionName) {
        return script.isSubscribe(remoteServer,subscriptionName);
    }

    @Override
    protected String subscribeImpl(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {
        return script.subscribe(remoteServer,opcServer, lstItemPath,rate);
    }

    @Override
    protected List<QualifiedValue> readSubscribeValuesImpl(String remoteServer, String subscriptionName) {
        return  script.readSubscribeValues(remoteServer,subscriptionName);
    }

    @Override
    protected boolean unsubscribeImpl(String remoteServer, String subscriptionName) {
        return script.unsubscribe(remoteServer,subscriptionName);
    }

    @Override
    protected void unsubscribeAllImpl(String remoteServer) {
        script.unsubscribeAll(remoteServer);
    }






}
