/**
 * AllJoyn Chat Android Sample code
 *
 * Implementation of AllJoyn interface.
 *
 * Copyright 2010-2011, Qualcomm Innovation Center, Inc.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.android.game.clash_of_the_balls.network;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusSignal;

import com.android.game.clash_of_the_balls.game.Vector;


/*
 * see 
 * https://www.alljoyn.org/docs-and-downloads/documentation/guide-alljoyn-development-using-java-sdk-rev-g#unique_23
 * for annotation syntax
 */
@BusInterface (name = "com.android.game.clash_of_the_balls.alljoyn")
public interface AlljoynInterface {
    /*
     * The BusSignal annotation signifies that this function should be used as
     * part of the AllJoyn interface.  The runtime is smart enough to figure
     * out that this is a used as a signal emitter and is only called to send
     * signals and not to receive signals.
     */
	
	/* client --> server */
    @BusSignal(signature="idd")
    public void sensorUpdate(double pos_x, double pos_y) throws BusException;
    
    //let the server know our well-known name
    @BusSignal
    public void clientInfoToServer(String well_known_name);
    
    /* server --> clients */
    
    @BusSignal
    public void gameCommand(byte[] data) throws BusException;
    
    //let all clients know the well-known name of a newly joined member
    @BusSignal
    public void clientInfoToClients(String unique_name, String well_known_name);
    
}
