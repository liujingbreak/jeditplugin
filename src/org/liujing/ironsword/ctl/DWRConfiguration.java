package org.liujing.ironsword.ctl;


import org.directwebremoting.fluent.FluentConfigurator;
/**
Use sprint-dwr configuration instead
*/
@Deprecated
public class DWRConfiguration extends FluentConfigurator{
    public void configure(){
        withConverterType("number", "org.liujing.ironsword.ctl.util.DWRNumberConverter");
        withConverter("number", "java.lang.Number");
        withConverter("bean", "org.liujing.ironsword.bean.ListPage");
        //withConverter("exception", "java.lang.Exception");
    }
}
