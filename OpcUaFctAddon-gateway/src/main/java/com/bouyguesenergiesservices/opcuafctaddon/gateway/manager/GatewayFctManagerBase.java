package com.bouyguesenergiesservices.opcuafctaddon.gateway.manager;

import com.bouyguesenergiesservices.opcuafctaddon.gateway.fct.rpc.GatewayFctRPCBase;
import com.bouyguesenergiesservices.opcuafctaddon.gateway_interface.IGatewayFctRPCBase;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.distributed.TagProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Unique Manager of GatewayFct (RPC / GAN)
 *
 * @deprecated
 * Created by regis on 13/07/2017.
 */
public final class GatewayFctManagerBase implements IGatewayFctRPCManagerBase {

    private static volatile GatewayFctManagerBase instance = null;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GatewayContext gatewayContext;

    //Managed RPC request
    private Map<String,GatewayFctRPCBase> mapSessionRPC = new HashMap<>();


    private GatewayFctManagerBase(GatewayContext gatewayContext){
        super();
        this.gatewayContext = gatewayContext;

    }

    /**
     * Singleton for this Gateway
     *
     * @param gatewayContext The context of this gateway
     * @return Singleton
     */
    public final static GatewayFctManagerBase getInstance(GatewayContext gatewayContext){
        if (GatewayFctManagerBase.instance == null){
            synchronized ((GatewayFctManagerBase.class)){
                if(GatewayFctManagerBase.instance == null){
                    GatewayFctManagerBase.instance = new GatewayFctManagerBase(gatewayContext);
                }
            }
        }
        return GatewayFctManagerBase.instance;
    }

    /**
     * Specific GatewayFctRPC associate to the session
     *
     * @param session Context of the client session
     * @return Create new 'GatewayFctRPC' if it is a new session Client
     */
    public IGatewayFctRPCBase getSessionFctRPC(ClientReqSession session){
        IGatewayFctRPCBase sessionFctRPC = null;
        if (session!=null){
            if (!mapSessionRPC.containsKey(session.getId())){
                //Create a GatewayFctRPC for each client
                logger.debug("getSessionFctRPC() > Create new 'GatewayFctRPC' in Manager sessionId:[{}]",session.getId());
                sessionFctRPC = new GatewayFctRPCBase(gatewayContext,session);
                mapSessionRPC.put(session.getId(), (GatewayFctRPCBase) sessionFctRPC);

            } else {
                logger.debug("getSessionFctRPC()> Get 'GatewayFctRPC' associate in Manager sessionId:[{}]",session.getId());
                sessionFctRPC = mapSessionRPC.get(session.getId());
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
                logger.debug("closeSessionFctRPC()> Suppress this client sessionId:[{}] in the GatewayFctManager",session.getId());
                mapSessionRPC.remove(session.getId());
            } else {
                logger.debug("closeSessionFctRPC()> Unknown sessionId:[{}]",session.getId());
            }
        }
    }

    /**
     * Notify all 'GatewayFctRPC' and  'GatewayFctGAN' to unsubscribe OPC item
     */
    public void allUnsubscribe(){
        if (mapSessionRPC.isEmpty()){
            logger.debug("allUnsubscribe()> There isn't session Local in this manager declare");
        }else {
            //Local (session open to manage session for local client request)
            mapSessionRPC.forEach((clientSession, sessionFctRPC) -> sessionFctRPC.unSubscribe());
            logger.debug("allUnsubscribe()> All subscription are clear foreach session 'IGatewayFctRPC' Local");
        }

    }

    /**
     * Notify all 'GatewayFctRPC' and  'GatewayFctGAN' to shutdown (release ressources) and remove all Item
     */
    public void shutdown(){

        if (mapSessionRPC.isEmpty()){
            logger.debug("shutdown() > There isn't session IGatewayFctRPCBase declare");
        }else {
            //Local (session open to manage session for local client request)
            mapSessionRPC.forEach((clientSession, sessionFctRPC) -> sessionFctRPC.shutdown());
            mapSessionRPC.clear();
            logger.debug("shutdown() > All subscription/timer are clear foreach session IGatewayFctRPCBase Local");
        }

    }


}
