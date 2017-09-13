package com.bouyguesenergiesservices.opcuafctaddon.gateway_interface;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Interface 'IGatewayFctRPC' add GAN options
 *
 * Created by regis on 07/08/2017.
 */
public interface IGatewayFctRPC extends IGatewayFctRPCBase {
    /**
     * Subscribe a list of OPC item
     *
     * @param remoteServer The name of the remoteServer gateway
     * @param opcServer Name of the OPC server (declare in gateway)
     * @param lstItemPath List of OPC item
     * @param rate Frequency OPC update subscription
     * @return True if the subscription is open
     */
    boolean subscribe(String remoteServer, String opcServer, List<String> lstItemPath, int rate);

    /**
     * unSubscribe all OPC item
     *
     * @param remoteServer The name of the remoteServer gateway
     */
    void unSubscribe(String remoteServer);





}
