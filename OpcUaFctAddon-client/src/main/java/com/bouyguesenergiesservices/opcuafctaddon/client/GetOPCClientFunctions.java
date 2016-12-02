package com.bouyguesenergiesservices.opcuafctaddon.client;

import com.bouyguesenergiesservices.opcuafctaddon.GetOPCRPC;
import com.bouyguesenergiesservices.opcuafctaddon.GetOPCScriptFunctions;
import com.inductiveautomation.ignition.client.gateway_interface.ModuleRPCFactory;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Created by regis on 18/10/2016.
 */
public class GetOPCClientFunctions extends GetOPCScriptFunctions {

    private final GetOPCRPC script;

    /**
     * Initialize an RPC proxies for communication between the client and the gateway.
     * GetOPCRPC Interface, it will generate a class that will forward calls to the module's RPC handler in the gateway, which should implement the interface.
     */
    public GetOPCClientFunctions(){
        script = ModuleRPCFactory.create("com.bouyguesenergiesservices.OpcUaFctAddon", GetOPCRPC.class);
    }

    /**
     * Call <B>readValues()</B> function in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstItemPath A list of strings, each representing an item path, or address to read from.
     *
     * @return QualifiedValue[] A sequence of objects, one for each address specified, in order. Each object will contains the value, quality, and timestamp returned from the OPC server for the corresponding address.
     */
    @Override
    protected List<QualifiedValue> readValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath) {
        return script.readValues(remoteServer,opcServer,lstItemPath);
    }


    /**
     * Call <B>isSubscribe()</B> function in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param subscriptionName UID of the subscription research
     *
     * @return boolean True, if the subscription is currently declare and managed.
     */
    @Override
    protected boolean isSubscribeImpl(String remoteServer, String subscriptionName) {
        return script.isSubscribe(remoteServer,subscriptionName);
    }


    /**
     * Call <B>subscribe()</B> function in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstItemPath A list of strings, each representing an item path, or address to read from.
     * @param rate Frequency of the subscription
     *
     * @return String UID of the subscription declare
     */
    @Override
    protected String subscribeImpl(String remoteServer, String opcServer, List<String> lstItemPath, int rate) {
        return script.subscribe(remoteServer,opcServer, lstItemPath,rate);
    }


    /**
     * Call <B>readSubscribeValues()</B> function in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param subscriptionName UID of the subscription research
     *
     * @return QualifiedValue[] A sequence of ALL objects (even unchanged object), one for each address specified, in order. Each object will contains the value, quality, and timestamp returned from the OPC server for the corresponding address.
     */
    @Override
    protected List<QualifiedValue> readSubscribeValuesImpl(String remoteServer, String subscriptionName) {
        return  script.readSubscribeValues(remoteServer,subscriptionName);
    }


    /**
     * Call <B>unsubscribe()</B> function in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param subscriptionName UID of the subscription research
     *
     * @return  boolean True, if subscription exist and unsubscribe
     */
    @Override
    protected boolean unsubscribeImpl(String remoteServer, String subscriptionName) {
        return script.unsubscribe(remoteServer,subscriptionName);
    }

    /**
     * Call <B>unsubscribeAll()</B> function in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     */
    @Override
    protected void unsubscribeAllImpl(String remoteServer) {
        script.unsubscribeAll(remoteServer);
    }






}
