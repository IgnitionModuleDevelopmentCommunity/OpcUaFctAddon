package com.bouyguesenergiesservices.gateway.service;

import com.inductiveautomation.ignition.common.logging.LogEvent;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.gan.security.TrialPeriodProtected;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.impl.services.annotations.FileStream;

import java.util.Date;
import java.util.List;

/**
 * Created by mattgross on 9/19/2016.
 */
//@TrialPeriodProtected(moduleId = "com.example.get-remote-logs")
public interface GetOPCService {

    String WRAPPER_ALLOWED_PROP = "wrapper-retrieve-allowed";
    String ACCESS_KEY = "wrapper-service-key";
    String SUCCESS_MSG = "SUCCESS";
    String FAIL_MSG = "FAIL";




    /***
     * Provides a List of QualifiedValue of Item Path to the calling gateway OPC connection declare.
     * @param opcServer Name of OPC connexion.
     * @param lstItemPath List of Itemp path to read.
     * @return a List of QualifiedValue, or an empty List if no opsServer are available.
     ***/

    List<QualifiedValue> getServiceReadValues(String opcServer, List<String> lstItemPath);

    boolean getServiceIsSubscribe(String subscriptionName);

    String getServiceSubscribe(String opcServer, List<String> lstItemPath, int rate);

    List<QualifiedValue> getServiceReadSubscribeValues(String subscriptionName);

    boolean getServiceUnsubscribe(String subscriptionName);

    void getServiceUnsubscribeAll();

}
