package com.bouyguesenergiesservices.designer;

import com.bouyguesenergiesservices.client.GetOPCClientFunctions;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;

import com.inductiveautomation.ignition.designer.model.DesignerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DesignerHook extends AbstractDesignerModuleHook {

    @Override
    public void startup(DesignerContext context, LicenseState activationState) throws Exception {
        super.startup(context, activationState);
    }


    @Override
    public void initializeScriptManager(ScriptManager manager){
        super.initializeScriptManager(manager);
        manager.addScriptModule( "system.byes.opc",
                new GetOPCClientFunctions(),
                new PropertiesFileDocProvider());
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

}
