package com.bouyguesenergiesservices.opcuafctaddon.gateway.manager;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan.GatewayFctGAN;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.rpc.GatewayFctRPC;
import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.gan.IGatewayFctGAN;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPC;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unique Manager of GatewayFct (RPC / GAN)
 *
 * Created by regis on 13/07/2017.
 */
public final class GatewayFctManager implements IGatewayFctGANManager,IGatewayFctRPCManager {

    private static volatile GatewayFctManager instance = null;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GatewayContext gatewayContext;

    //Managed RPC request
    private ConcurrentHashMap<String,GatewayFctRPC> mapSessionRPC = new ConcurrentHashMap<>();

    //Managed GAN request
    private ConcurrentHashMap<String,GatewayFctGAN> mapSessionGAN = new ConcurrentHashMap<>();



    private GatewayFctManager(GatewayContext gatewayContext){
        super();
        this.gatewayContext = gatewayContext;

    }


    /**
     * Singleton for this Gateway
     *
     * @param gatewayContext The context of this gateway
     * @return Singleton
     */
    public final static GatewayFctManager getInstance(GatewayContext gatewayContext){
        if (GatewayFctManager.instance == null){
            synchronized ((GatewayFctManager.class)){
                if(GatewayFctManager.instance == null){
                    GatewayFctManager.instance = new GatewayFctManager(gatewayContext);
                }
            }
        }
        return GatewayFctManager.instance;
    }

    /**
     * Specific GatewayFctRPC associate to the session
     *
     * @param session Context of the client session
     * @return Create new 'GatewayFctRPC' if it is a new session Client
     */
    public IGatewayFctRPC getSessionFctRPC(ClientReqSession session){
        IGatewayFctRPC sessionFctRPC = null;
        if (session!=null){
            if (!mapSessionRPC.containsKey(session.getId())){
                //Create a GatewayFctRPC for each client
                logger.debug("getSessionFctRPC() > Create new 'GatewayFctRPC' in Manager sessionId:[{}]",session.getId());
                sessionFctRPC = new GatewayFctRPC(gatewayContext,session);
                mapSessionRPC.put(session.getId(), (GatewayFctRPC) sessionFctRPC);

            } else {
                logger.trace("getSessionFctRPC()> Get 'GatewayFctRPC' associate in Manager sessionId:[{}]",session.getId());
                sessionFctRPC = mapSessionRPC.get(session.getId());
            }
        }

        return sessionFctRPC;
    }


    /**
     * Specific GatewayFctRPC associate to the session
     *
     * @param session Context of the client session
     * @return Create new 'GatewayFctRPC' if it is a new session Client
     */
    public IGatewayFctRPC getSessionFctRPC(String session){
        IGatewayFctRPC sessionFctRPC = null;
        if (session!=null){
            if (mapSessionRPC.containsKey(session)){
                logger.trace("getSessionFctRPC()> Get 'GatewayFctRPC' associate in Manager sessionId:[{}]",session);
                sessionFctRPC = mapSessionRPC.get(session);
            } else {
                logger.debug("getSessionFctRPC()> Unknown sessionId:[{}]",session);
            }
        }
        return sessionFctRPC;
    }





    /**
     * Close 'GatewayFctRPC' associate to the session RPC
     *
     * @param session Context of the client session
     */
    public void closeSessionFctRPC(ClientReqSession session){

        if (!mapSessionRPC.isEmpty()){
            //notify session for close connexion
            if (mapSessionRPC.containsKey(session.getId())) {
                logger.trace("closeSessionFctRPC()> Shutdown this client sessionId:[{}]",session.getId());

                IGatewayFctRPC sessionFctRPC = mapSessionRPC.get(session.getId());
                sessionFctRPC.shutdown();
                mapSessionRPC.remove(session.getId());
            } else {
                logger.debug("closeSessionFctRPC()> Unknown sessionId:[{}]",session.getId());
            }
        }
    }

    /**
     * Specific 'GatewayFctGAN' associate to the session declare through the GAN
     *
     * @param remoteServer Name of the remote gateway
     * @param session Id client session in the remote gateway
     * @return Create new 'GatewayFctGAN' if it is a new session Client declare through the GAN
     */
    public IGatewayFctGAN getSessionFctGAN(String remoteServer, String session){
        IGatewayFctGAN sessionFctGAN = null;

        if (session!=null && remoteServer!= null){
            if (mapSessionGAN.containsKey(remoteServer+session)){

                logger.trace("getSessionFctGAN()> Get 'GatewayFctGAN' associate in Manager remoteServer:[{}] sessionId:[{}]",remoteServer, session);
                sessionFctGAN = mapSessionGAN.get(remoteServer+session);
            } else {

                //Create a GatewayFctGAN for each client
                logger.debug("getSessionFctGAN() > Create new 'GatewayFctGAN' in Manager remoteServer:[{}] sessionId:[{}]",remoteServer, session);
                sessionFctGAN = new GatewayFctGAN(gatewayContext,session,remoteServer);
                mapSessionGAN.put(remoteServer+session, (GatewayFctGAN) sessionFctGAN);

            }
        }

        return sessionFctGAN;
    }




    /**
     * Close 'GatewayFctGAN' associate to the session declare through the GAN
     *
     * @param session Id client session in the remote gateway
     */
    public void closeSessionFctGAN(String remoteServer, String session){
        if (!mapSessionGAN.isEmpty()){
            //notify session for close connexion
            if (mapSessionGAN.containsKey(remoteServer+session)) {
                logger.trace("closeSessionFctGAN()> Shutdown this client remoteServer:[{}] sessionId:[{}]",remoteServer, session);
                IGatewayFctGAN sessionFctGAN = mapSessionGAN.get(remoteServer+session);
                sessionFctGAN.shutdown();
                mapSessionGAN.remove(remoteServer+session);

            } else {
                logger.debug("closeSessionFctGAN()> Unknown remoteServer:[{}] sessionId:[{}]",remoteServer, session);
            }
        }
    }

    /**
     * Notify all 'GatewayFctRPC' and  'GatewayFctGAN' to unsubscribe OPC item
     */
    public void allUnsubscribe(){

        if (mapSessionRPC.isEmpty()) {
            logger.trace("allUnsubscribe()> There isn't session Local in this manager declare");
        } else {
            //Local (session open to manage session for local client request)
            mapSessionRPC.forEach((clientSession, sessionFctRPC) -> sessionFctRPC.unSubscribe());
            logger.debug("allUnsubscribe()> All subscription are clear foreach session 'IGatewayFctRPC' Local");
        }
        if (mapSessionGAN.isEmpty()) {
            logger.trace("allUnsubscribe()> There isn't session GAN in this manager declare");
        } else {
            //GAN (session open to manage session for remoteServer request)
            mapSessionGAN.forEach((string, sessionFctGAN) -> sessionFctGAN.unSubscribe());
            logger.debug("allUnsubscribe()> All subscription are clear foreach session 'IGatewayFctRPC' Remote");
        }

    }

    /**
     * Notify all 'GatewayFctRPC' and  'GatewayFctGAN' to shutdown (release ressources) and remove all Item
     */
    public void shutdownAll(){

        if (mapSessionRPC.isEmpty()){
            logger.trace("shutdown() > There isn't session IGatewayFctRPCBase declare");
        }else {
            //Local (session open to manage session for local client request)
            mapSessionRPC.forEach((clientSession, sessionFctRPC) -> sessionFctRPC.shutdown());
            mapSessionRPC.clear();
            logger.debug("shutdown() > All subscription/timer are clear foreach session IGatewayFctRPCBase Local");
        }
        if (mapSessionGAN.isEmpty()){
            logger.trace("shutdown() > There isn't session IGatewayFctRPC declare");
        }else {
            //GAN (session open to manage session for remoteServer request)
            mapSessionGAN.forEach((string, sessionFctGAN)-> sessionFctGAN.shutdown());
            mapSessionGAN.clear();
            logger.debug("shutdown() > All subscription/timer are clear foreach session IGatewayFctRPC Remote");
        }

    }


    public String toString(){
        return String.format("mapSessionRPC size:[%s] mapSessionGAN size:[%s] ",mapSessionRPC.size(),mapSessionGAN.size());
    }


}
