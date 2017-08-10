package com.bouyguesenergiesservices.opcuafctaddon.client;


import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHook extends AbstractClientModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ClientContext clientContext = null;
    private ClientFct clientScriptOPC = null;


    @Override
    public void startup(ClientContext context, LicenseState activationState) throws Exception {
        super.startup(context,activationState);
        this.clientContext = context;
    }


    /**
     * Initialize a newly-instantiated script manager.
     * This will be called exactly once.
     * Add a ScriptModule instance "system.byes.opc" to manage by the Client Instance.
     *
     * @param manager An available manager create by Client
     */
    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);

        clientScriptOPC = new ClientFct(clientContext);
        manager.addScriptModule(
                "byes.opcuafctaddon",
                clientScriptOPC,
                new PropertiesFileDocProvider());

        logger.trace("initializeScriptManager()");

    }


    @Override
    public void shutdown() {
       //Unsubscribe this client
        logger.trace("shutdown()");
        if (clientScriptOPC != null) {
            clientScriptOPC.shutdown();
        }
    }

}
