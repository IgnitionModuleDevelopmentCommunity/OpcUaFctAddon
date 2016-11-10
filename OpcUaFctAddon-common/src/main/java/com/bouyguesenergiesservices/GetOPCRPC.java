package com.bouyguesenergiesservices;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Created by regis on 18/10/2016.
 *
 */
public interface GetOPCRPC {

    List<QualifiedValue> readValues(String remoteServer,String opcServer, List<String> lstItemPath);

    boolean isSubscribe(String remoteServer,String subscriptionName);

    String subscribe(String remoteServer,String opcServer, List<String> lstItemPath, int rate);

    List<QualifiedValue> readSubscribeValues(String remoteServer,String subscriptionName);

    boolean unsubscribe(String remoteServer,String subscriptionName);

    void unsubscribeAll(String remoteServer);

}
