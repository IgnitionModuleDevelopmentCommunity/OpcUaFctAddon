package com.bouyguesenergiesservices.opcuafctaddon.gateway_interface;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Interface 'GatewayFct' between Client / Gateway
 * Created by regis on 10/07/2017.
 */
public interface IGatewayFctRPCBase {

    /**
     * Subscribe a list of OPC item
     * @param opcServer Name of the OPC server (declare in gateway)
     * @param lstItemPath List of OPC item
     * @param rate Frequency OPC update subscription
     * @return True if the subscription is open
     */
    boolean subscribe(String opcServer, List<String> lstItemPath, int rate);

    /**
     * Unsubscribe all OPC item
     */
    void unSubscribe();

    /**
     * Notify the gateway that this connection RPC (client) is shutdown
     */
    void notifyShutdown();

}
