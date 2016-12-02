package com.bouyguesenergiesservices.opcuafctaddon.gateway.service;


import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Interface to Manage All OPC functions expose through the GAN
 * Methods in this interface can be called by Gateway
 * opcService.getService...()
 *
 * Created by regis on 18/10/2016.
 *
 */
public interface GetOPCService {

    /**
     * Equivalent to the system.opc.readValue in the GAN.
     *
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstItemPath A list of strings, each representing an item path, or address to read from.
     *
     * @return QualifiedValue[] A sequence of objects, one for each address specified, in order. Each object will contains the value, quality, and timestamp returned from the OPC server for the corresponding address.
     */
    List<QualifiedValue> getServiceReadValues(String opcServer, List<String> lstItemPath);

    /**
     * Check if the specify Subscription Name is currently exist in the  GAN.
     *
     * @param subscriptionName UID of the subscription research
     *
     * @return boolean True, if the subscription is currently declare and managed else False.
     */
    boolean getServiceIsSubscribe(String subscriptionName);

    /**
     * Declare an OPC Subscription (through the GAN) with lstItemPath (only integer type).
     * Call cyclically the readSubscribeValues to refresh all values.
     *
     * @param opcServer The name of the OPC server connection in which the items reside.
     * @param lstItemPath A list of strings, each representing an item path, or address to read from.
     * @param rate Frequency of the subscription
     *
     * @return String UID of the subscription declare
     */
    String getServiceSubscribe(String opcServer, List<String> lstItemPath, int rate);

    /**
     * Get all last OPC values states of lstItemPath subscribe before subscriptionName in the GAN.
     * If there is no change until the previous calling, None is return.
     * If a subscription isn't read with "readSubscribeValues" function 30 seconds later, it is automatically unsubscribe (Timeout).
     *
     * @param subscriptionName UID of the subscription research
     *
     * @return QualifiedValue[] A sequence of ALL objects (even unchanged object), one for each address specified, in order. Each object will contains the value, quality, and timestamp returned from the OPC server for the corresponding address.
     */
    List<QualifiedValue> getServiceReadSubscribeValues(String subscriptionName);


    /**
     * Unsubscribe a Subscription on the GAN.
     *
     * @param subscriptionName UID of the subscription research
     *
     * @return  boolean True, if subscription exist and unsubscribe else False
     */
    boolean getServiceUnsubscribe(String subscriptionName);

    /**
     * Unsubscribe ALL Item subscribe before in the GAN.
     */
    void getServiceUnsubscribeAll();

}
