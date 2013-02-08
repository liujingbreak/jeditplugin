package org.liujing.ironsword.ctl.util;

import org.directwebremoting.convert.PrimitiveConverter;
import org.directwebremoting.extend.*;

public class DWRNumberConverter extends PrimitiveConverter{
    public Object convertInbound(java.lang.Class<?> paramType, InboundVariable data){
        if(paramType == Number.class){
            return Integer.valueOf(data.getValue());
        }else{
            return super.convertInbound(paramType, data);
        }
    }
}
