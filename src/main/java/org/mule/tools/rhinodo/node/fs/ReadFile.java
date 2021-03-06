/**
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.rhinodo.node.fs;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

public class ReadFile extends BaseFunction {
    private Queue<Function> asyncCallbacksQueue;

    public ReadFile(Queue<Function> asyncCallbacksQueue) {
        this.asyncCallbacksQueue = asyncCallbacksQueue;
    }




    private Object readFile(final Context cx, final Scriptable scope, final Scriptable thisObj,
                            String file, String encoding,final Function callback) {

        final String result;
        try {
            result = FileUtils.readFileToString(new File(file).getAbsoluteFile(), encoding);
        } catch (IOException e) {
            if (callback != null) {
                asyncCallbacksQueue.add(new BaseFunction() {

                    @Override
                    public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                        return callback.call(cx, scope, thisObj, new Object[] {true});
                    }
                });
            }

            return Undefined.instance;
        }

        if (callback != null) {
            asyncCallbacksQueue.add(new BaseFunction() {

                @Override
                public Object call(Context cx2, Scriptable scope2, Scriptable thisObj2, Object[] args2) {
                    return callback.call(cx, scope, thisObj, new Object[] {null, result});
                }
            });
        }

        return Undefined.instance;
    }


    @Override
    public Object call(final Context cx,final Scriptable scope, final Scriptable thisObj, Object[] args) {
        if (args.length == 2) {
            final String file = Context.toString(args[0]);
            final Function callback = (Function) args[1];

            return readFile(cx,scope,thisObj,file,"UTF-8",callback);
        } else if ( args.length == 3) {
            final String file = Context.toString(args[0]);
            String encoding = Context.toString(args[1]);
            final Function callback = (Function) args[2];

            return readFile(cx,scope,thisObj,file,encoding,callback);
        } else {
            throw new RuntimeException("Only readFile with 3 parameters supported");
        }
    }
}
