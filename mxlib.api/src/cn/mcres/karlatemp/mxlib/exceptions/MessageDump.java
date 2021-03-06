/*
 * Copyright (c) 2018-2019 Karlatemp. All rights reserved.
 * Reserved.FileName: MessageDump.java@author: karlatemp@vip.qq.com: 19-9-18 下午5:54@version: 2.0
 */

package cn.mcres.karlatemp.mxlib.exceptions;

public class MessageDump extends Throwable {
    public MessageDump() {
    }

    public MessageDump(String message) {
        super(message);
    }

    public MessageDump(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageDump(Throwable cause) {
        super(cause);
    }

    public MessageDump(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static MessageDump create(String msg) {
        return new MessageDump(msg, null, true, false);
    }

    public static MessageDump create(String msg, Throwable cause) {
        return new MessageDump(msg, cause, true, false);
    }
}
