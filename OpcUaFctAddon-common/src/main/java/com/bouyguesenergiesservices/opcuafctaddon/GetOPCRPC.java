package com.bouyguesenergiesservices.opcuafctaddon;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Interface to Expose All OPC RPC functions
 * Methods in this interface can be called by clients and designers by using
 * ModuleRPCFactory.create()
 *
 * Created by regis on 18/10/2016.
 *
 */
public interface GetOPCRPC {

    /**
     * Equivalent to the system.opc.readValue in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstItemPath A list of strings, each representing an item path, or address to read from.
     *
     * @return QualifiedValue[] A sequence of objects, one for each address specified, in order. Each object will contains the value, quality, and timestamp returned from the OPC server for the corresponding address.
     */
    List<QualifiedValue> readValues(String remoteServer,String opcServer, List<String> lstItemPath);

    /**
     * Check if the specify Subscription Name is currently exist in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param subscriptionName UID of the subscription research
     *
     * @return boolean True, if the subscription is currently declare and managed else False.
     */
    boolean isSubscribe(String remoteServer,String subscriptionName);

    /**
     * Declare an OPC Subscription (locally or through the GAN) with lstItemPath (only integer type).
     * Call cyclically the readSubscribeValues to refresh all values.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstItemPath A list of strings, each representing an item path, or address to read from.
     * @param rate Frequency of the subscription
     *
     * @return String UID of the subscription declare
     */
    String subscribe(String remoteServer,String opcServer, List<String> lstItemPath, int rate);

    /**
     * Get all last OPC values states of lstItemPath subscribe before subscriptionName in the local gateway for dispatching to Local or GAN.
     * If there is no change until the previous calling, None is return.
     * If a subscription isn't read with "readSubscribeValues" function 30 seconds later, it is automatically unsubscribe (Timeout).
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param subscriptionName UID of the subscription research
     *
     * @return QualifiedValue[] A sequence of ALL objects (even unchanged object), one for each address specified, in order. Each object will contains the value, quality, and timestamp returned from the OPC server for the corresponding address.
     */
    List<QualifiedValue> readSubscribeValues(String remoteServer,String subscriptionName);

    /**
     * Unsubscribe a Subscription on the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     * @param subscriptionName UID of the subscription research
     *
     * @return  boolean True, if subscription exist and unsubscribe else False
     */
    boolean unsubscribe(String remoteServer,String subscriptionName);

    /**
     * Unsubscribe ALL Item subscribe before in the local gateway for dispatching to Local or GAN.
     *
     * @param remoteServer Name of the remote Server set in the Gateway Settings system Name
     */
    void unsubscribeAll(String remoteServer);

}
