package org.liujing.ironsword.ctl;

import java.util.*;
import java.io.*;
import java.util.logging.*;

public interface ControllerProgressMonitor{
    void stateMessage(String msg);
    void state(int number);
}
